(ns leihs.my.password-restore
  (:require [clj-time
             [core :as clj-time]
             [local :as clj-time-local]]
            [clj-ulid :refer [random-base32-string]]
            [clojure.java.jdbc :as jdbc]
            [clojure.spec.alpha :as spec]
            [compojure.core :as cpj]
            [leihs.core
             [core :refer [presence]]
             [ds :as ds]
             [sql :as sql]
             [ssr :as ssr]]
            [leihs.my.paths :refer [path]]
            [leihs.my.resources.settings.back :as settings]
            [leihs.my.sign-in.back :refer [error-flash-invalid-user]]
            [leihs.my.user.shared :refer [set-password]]))

(spec/def ::external-base-url presence)

(defn email-content
  [tx token]
  (clojure.string/join "\n"
                       ["To do Password reset click this link:"
                        (str (->> tx
                                  settings/settings!
                                  :external_base_url
                                  (spec/assert ::external-base-url))
                             "/reset-password?token="
                             token)
                        ""
                        (str "Or type the token: " token)
                        ""
                        "If you did not request this, you can just ignore it."
                        "Learn more: https://docs.leihs.app/passwort-reset"]))

(defn make-token [n] (clojure.string/upper-case (random-base32-string n)))

(defn normalize-token-str
  [str]
  (clojure.string/escape (clojure.string/upper-case str) {\O 0, \I 1, \L 1}))

(defn get-user-by-login-or-email
  [tx login-or-email]
  (-> (sql/select :*)
      (sql/from :users)
      (sql/merge-where [:or [:= :login login-or-email]
                        [:= :email login-or-email]])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn insert-into-emails
  [tx user token]
  (-> (sql/insert-into :emails)
      (sql/values [{:user_id (:id user),
                    :subject "Password reset",
                    :body (email-content tx token),
                    :from_address (-> tx
                                      settings/settings!
                                      :smtp_default_from_address)}])
      sql/format
      (->> (jdbc/execute! tx))))

(defn insert-into-user-password-resets
  [tx user user-param token]
  (-> (sql/insert-into :user_password_resets)
      (sql/values [{:user_id (:id user),
                    :token token,
                    :used_user_param user-param,
                    :valid_until (sql/raw "now() + interval '1 hour'")}])
      sql/format
      (->> (jdbc/execute! tx))))

(defn get-from-user-password-resets
  [tx token-param]
  (-> (sql/select :*)
      (sql/from :user_password_resets)
      (sql/where [:= (normalize-token-str token-param) :token])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn forgot-get
  [request]
  (let [user-param (-> request
                       :params
                       :user)
        tx (:tx request)
        user (get-user-by-login-or-email tx user-param)]
    (if (-> user
            :email
            presence)
      {:headers {"Content-Type" "text/html"},
       :body (ssr/render-page-by-name request
                                      "PasswordForgotPage"
                                      {:userParam user-param})}
      {:headers {"Content-Type" "text/html"},
       :status 422,
       :body "user does not have an email"})))

(defn forgot-post
  [request]
  (let [user-param (-> request
                       :params
                       :user)
        tx (:tx request)
        user (get-user-by-login-or-email tx user-param)
        token (make-token 20)]
    (if user
      (do (insert-into-user-password-resets tx user user-param token)
          (insert-into-emails tx user token)
          {:headers {"Content-Type" "text/html"},
           :body (ssr/render-page-by-name request
                                          "PasswordForgotSuccessPage"
                                          {:userParam user-param,
                                           :message "check your email!",
                                           :resetPwLink "/reset-password"})})
      {:headers {"Content-Type" "text/html"},
       :status 404,
       :body (ssr/render-page-by-name request
                                      "PasswordForgotPage"
                                      {:userParam user-param,
                                       :flashMessages
                                         [error-flash-invalid-user]})})))


(defn reset-get
  [request]
  (let [token-param (-> request
                        :params
                        :token)
        tx (:tx request)
        user-password-reset (some->> token-param
                                     (get-from-user-password-resets tx))]
    (if (and token-param (not user-password-reset))
      {:headers {"Content-Type" "text/html"},
       :status 422
       :body "the token is invalid"}
      {:headers {"Content-Type" "text/html"},
       :body (ssr/render-page-by-name
               request
               "PasswordResetPage"
               {:pwReset {:userParam (:used_user_param user-password-reset),
                          :token token-param}})})))

(defn get-user-with-token
  [tx token]
  (-> (sql/select :*)
      (sql/from :users)
      (sql/join :user_password_resets
                [:= :user_password_resets.user_id :users.id])
      (sql/merge-where [:= true :users.account_enabled])
      (sql/merge-where [:= true :users.password_sign_in_enabled])
      (sql/merge-where [:= :user_password_resets.token
                        (normalize-token-str token)])
      sql/format
      (->> (jdbc/query tx))
      first))

(defn reset-post
  [request]
  (let [tx (:tx request)
        token (-> request
                  :params
                  :token)
        password (-> request
                     :params
                     :newPassword)
        password-reset (get-from-user-password-resets tx token)
        user (get-user-with-token tx token)]
    (-> (cond
          (not password-reset) {:status 404,
                                :body "password reset for the token not found"}
          (clj-time/after? (clj-time-local/local-now)
                           (:valid_until password-reset))
            {:status 403, :body "the token has expired"}
          (not user) {:status 404,
                      :body "user enabled for password auth not found"}
          :else (do (set-password (:id user) password tx)
                    ; NOTE: row from user_password_resets deleted by trigger
                    {:body (ssr/render-page-by-name
                             request
                             "PasswordResetSuccessPage"
                             {})}))
        (assoc :headers {"Content-Type" "text/html"}))))

(def forgot-routes
  (cpj/routes (cpj/GET (path :forgot-password) [] #'forgot-get)
              (cpj/POST (path :forgot-password) [] #'forgot-post)))

(def reset-routes
  (cpj/routes (cpj/GET (path :reset-password) [] #'reset-get)
              (cpj/POST (path :reset-password) [] #'reset-post)))

(comment (-> (sql/select :*)
             (sql/from :user_password_resets)
             (sql/format)
             (->> (jdbc/query (ds/get-ds))))
         (clj-time-local/local-now)
         (def valid-until
           (-> (ds/get-ds)
               (get-from-user-password-resets "X6JEHQ4X3ZR8EGKNBVQ6")
               :valid_until))
         (clj-time/after? (clj-time-local/local-now) valid-until))
