(ns leihs.my.password-restore.back
  (:require
   [clojure.spec.alpha :as spec]
   [clojure.string]
   [compojure.core :as cpj]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.core.anti-csrf.back :refer [anti-csrf-props]]
   [leihs.core.core :refer [presence]]
   [leihs.core.random :refer [base32-crockford-rand-str]]
   [leihs.core.release :as release]
   [leihs.core.remote-navbar.shared :refer [navbar-props]]
   [leihs.my.back.html :refer [auth-page]]
   [leihs.my.paths :refer [path]]
   [leihs.my.user.shared :refer [set-password]]
   [next.jdbc :as jdbc]
   [tick.core :as tick]))

(spec/def ::external-base-url presence)
(spec/def ::smtp_default_from_address presence)

(defn email-content
  [token {settings :settings}]
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

(defn make-token [n] (base32-crockford-rand-str n))

(defn normalize-token-str
  [str]
  (clojure.string/escape (clojure.string/upper-case str) {\O 0, \I 1, \L 1}))

(defn insert-into-emails [token {user-id :id email :email} {settings :settings tx :tx :as request}]
  (-> (sql/insert-into :emails)
      (sql/values [{:user_id user-id,
                    :to_address email,
                    :subject "Password reset",
                    :body (email-content token request),
                    :from_address (->> settings
                                       :smtp_default_from_address
                                       (spec/assert ::smtp_default_from_address))}])
      sql-format
      (->> (jdbc/execute! tx))))

(defn insert-into-user-password-resets
  [token {user-id :id} {{user-param :user} :params tx :tx}]
  (-> (sql/insert-into :user_password_resets)
      (sql/values [{:user_id [:cast user-id :uuid],
                    :token token,
                    :used_user_param user-param,
                    :valid_until [:+ [:now] [:interval "1 hour"]]}])
      sql-format
      (->> (jdbc/execute! tx))))

(defn get-from-user-password-resets
  [tx token-param]
  (-> (sql/select :*)
      (sql/from :user_password_resets)
      (sql/where [:= (normalize-token-str token-param) :token])
      sql-format
      (->> (jdbc/execute-one! tx))))

(defn common-props [request]
  (as-> request <>
    (navbar-props <>)
    (hash-map :navbar <>)
    (merge (anti-csrf-props request) <>)
    (merge {:footer {:appVersion release/version}} <>)))

(defn user-with-unique-id [tx user-unique-id]
  (-> (sql/select :*)
      (sql/from :users)
      (sql/where [:or [:= :users.org_id user-unique-id]
                  [:= :users.login user-unique-id]
                  [:= [:lower :users/email] [:lower (-> user-unique-id (or ""))]]])
      sql-format
      (->> (jdbc/execute-one! tx))))

(defn forgot-get
  [request]
  (let [user-param (-> request
                       :params
                       :user)
        tx (:tx request)
        user (user-with-unique-id tx user-param)]
    (cond
      (-> request :settings :email_sending_enabled not)
      {:status 422,
       :headers {"Content-Type" "text/html"},
       :body (-> request
                 common-props
                 (merge {:userParam user-param,
                         :flashMessages [{:messageID "password_forgot_email_sending_disabled_text"
                                          :level "error"}]})
                 auth-page)}

      (-> user :email presence not)
      {:status 422,
       :headers {"Content-Type" "text/html"},
       :body (-> request
                 common-props
                 (merge {:userParam user-param,
                         :flashMessages [{:messageID "password_forgot_user_has_no_email_flash_text"
                                          :level "error"}]})
                 auth-page)}

      :else
      {:headers {"Content-Type" "text/html"}
       :body (-> request
                 common-props
                 (merge {:userParam user-param})
                 auth-page)})))

(defn forgot-post [{{user-param :user} :params tx :tx :as request}]
  (let [user (user-with-unique-id tx user-param)
        token (make-token 20)]
    (if user
      (do (insert-into-user-password-resets token user request)
          (insert-into-emails token user request)
          {:headers {"Content-Type" "text/html"},
           :body (-> request
                     common-props
                     (merge {:status "success"
                             :userParam user-param,
                             :messageID "password_forgot_check_email_message",
                             :resetPwLink "/reset-password",
                             :resetPwLinkTextID "password_reset_link_test"})
                     auth-page)})

      {:headers {"Content-Type" "text/html"},
       :status 404,
       :body (-> request
                 common-props
                 (merge {:userParam user-param,
                         :flashMessages [{:messageID "sign_in_invalid_user_flash_message"
                                          :level "error"}]})
                 auth-page)})))

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
       :body (-> request
                 common-props
                 (merge {:pwReset {:userParam (:used_user_param user-password-reset),
                                   :token token-param}})
                 auth-page)})))

(defn get-user-with-token
  [tx token]
  (-> (sql/select :*)
      (sql/from :users)
      (sql/join :user_password_resets
                [:= :user_password_resets.user_id :users.id])
      (sql/where [:and [:= true :users.account_enabled]
                  [:= true :users.password_sign_in_enabled]
                  [:= :user_password_resets.token (normalize-token-str token)]])
      sql-format
      (->> (jdbc/execute-one! tx))))

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
          :else (do (set-password (:user_id user) password tx)
                    ; NOTE: row from user_password_resets deleted by trigger
                    {:body (-> request
                               common-props
                               (merge {:status "success"})
                               auth-page)}))
        (assoc :headers {"Content-Type" "text/html"}))))

(def forgot-routes
  (cpj/routes (cpj/GET (path :forgot-password) [] #'forgot-get)
              (cpj/POST (path :forgot-password) [] #'forgot-post)))

(def reset-routes
  (cpj/routes (cpj/GET (path :reset-password) [] #'reset-get)
              (cpj/POST (path :reset-password) [] #'reset-post)))
