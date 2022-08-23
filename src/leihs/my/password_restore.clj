(ns leihs.my.password-restore
  (:require
    [tick.core :as tick]
    [clj-ulid :refer [random-base32-string]]
    [clojure.java.jdbc :as jdbc]
    [clojure.spec.alpha :as spec]
    [compojure.core :as cpj]
    [leihs.core.core :refer [presence]]
    [leihs.core.db :as db]
    [leihs.core.settings :as settings]
    [leihs.core.sign-in.back :refer [user-with-unique-id]]
    [leihs.core.sql :as sql]
    [leihs.core.ssr :as ssr]
    [leihs.my.paths :refer [path]]
    [leihs.my.user.shared :refer [set-password]]
    [taoensso.timbre :refer [debug info warn error spy]]
    ))

(spec/def ::external-base-url presence)
(spec/def ::smtp_default_from_address presence)

(defn email-content
  [token {tx :tx settings :settings}]
  (clojure.string/join "\n"
                       ["To do Password reset click this link:"
                        (str (->> settings :external_base_url (spec/assert ::external-base-url))
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

(defn insert-into-emails [token user {settings :settings tx :tx :as request}]
  (-> (sql/insert-into :emails)
      (sql/values [{:user_id (:id user),
                    :subject "Password reset",
                    :body (email-content token request),
                    :from_address (->> settings
                                       :smtp_default_from_address
                                       (spec/assert ::smtp_default_from_address))}])
      sql/format
      (->> (jdbc/execute! tx))))

(defn insert-into-user-password-resets
  [token user {{user-param :user} :params tx :tx :as request}]
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

(def error-flash-user-has-no-email
  {:level "error",
   :message
     (clojure.string/join
       " \n"
       ["Keine Email-Adresse vorhanden!"
        "Das Passwort für dieses Benutzerkonto kann nicht zurückgesetzt werden,
         weil keine Email-Adresse im System vorhanden ist.
         Bitte prüfen Sie den angegebenen Benutzernamen.
         Kontaktieren Sie den leihs-Support, falls das Problem weiterhin besteht."])})

(defn forgot-get
  [request]
  (let [user-param (-> request
                       :params
                       :user)
        tx (:tx request)
        user (user-with-unique-id tx user-param)
        headers {:headers {"Content-Type" "text/html"}}]
    (cond
      (-> request :settings :email_sending_enabled not)
      {:status 422,
       :headers {"Content-Type" "text/html"},
       :body (ssr/render-page-by-name
               request
               "PasswordForgotPage"
               {:userParam user-param,
                :flashMessages [{:messageID "password_forgot_email_sending_disabled_text"
                                 :level "error"}]})}

      (-> user :email presence not)
      {:status 422,
       :headers {"Content-Type" "text/html"},
       :body (ssr/render-page-by-name
               request
               "PasswordForgotPage"
               {:userParam user-param,
                :flashMessages [{:messageID "password_forgot_user_has_no_email_flash_text"
                                 :level "error"}]})}

      :else
      {:headers {"Content-Type" "text/html"},
       :body (ssr/render-page-by-name request
                                      "PasswordForgotPage"
                                      {:userParam user-param})})))

(defn forgot-post [{{user-param :user} :params tx :tx :as request}]
  (let [user (user-with-unique-id tx user-param)
        token (make-token 20)]
    (if user
      (do (insert-into-user-password-resets token user request)
          (insert-into-emails token user request)
          {:headers {"Content-Type" "text/html"},
           :body (ssr/render-page-by-name
                   request
                   "PasswordForgotSuccessPage"
                   {:userParam user-param,
                    :messageID "password_forgot_check_email_message",
                    :resetPwLink "/reset-password",
                    :resetPwLinkTextID "password_reset_link_test"})})
      {:headers {"Content-Type" "text/html"},
       :status 404,
       :body (ssr/render-page-by-name
               request
               "PasswordForgotPage"
               {:userParam user-param,
                :flashMessages [{:messageID "sign_in_invalid_user_flash_message"
                                 :level "error"}]})})))

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
          (tick/> (tick/now) (:valid_until password-reset))
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

