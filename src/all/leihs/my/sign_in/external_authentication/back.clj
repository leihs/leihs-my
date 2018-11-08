(ns leihs.my.sign-in.external-authentication.back
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.core.sql :as sql]
    [leihs.core.auth.session :as session]

    [leihs.my.env :as env]
    [leihs.my.back.html :as html]
    [leihs.my.paths :refer [path]]
    [leihs.my.sign-in.shared 
     :refer [auth-system-user-base-query 
             auth-system-base-query-for-unique-id]]

    [buddy.core.keys :as keys]
    [buddy.sign.jwt :as jwt]
    [clj-time.core :as time]
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect]]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]))

(defn auth-system-user-query [user-unique-id authentication-system-id]
  (-> (auth-system-base-query-for-unique-id user-unique-id authentication-system-id)
      (sql/merge-select 
        [(sql/call :row_to_json :authentication_systems) :authentication_system]
        [(sql/call :row_to_json :users) :user])
      sql/format))

(defn authentication-system-user-data! 
  [user-unique-id authentication-system-id tx]
  (if-let [authentication_system_and_user 
           (->> (auth-system-user-query 
                  user-unique-id authentication-system-id)
                (jdbc/query tx) first)]
    (merge authentication_system_and_user
           (->> (-> (sql/select :*)
                    (sql/from :authentication_systems_users)
                    (sql/merge-where [:= :authentication_systems_users.authentication_system_id 
                                      (-> authentication_system_and_user :authentication_system :id)])
                    (sql/merge-where [:= :authentication_systems_users.user_id
                                      (-> authentication_system_and_user :user_id:id)])
                    sql/format)
                (jdbc/query tx) first))
    (or (throw (ex-info
                 "External authentication system not found or not enabled" 
                 {:status 500})))))

(defn prepare-key-str [s]
  (->> (-> s (clojure.string/split #"\n"))
       (map clojure.string/trim)
       (map presence)
       (filter identity)
       (clojure.string/join "\n")))

(defn private-key! [s]
  (-> s prepare-key-str keys/str->private-key
      (or (throw
            (ex-info "Private key error!" 
                     {:status 500})))))

(defn public-key! [s]
  (-> s prepare-key-str keys/str->public-key
      (or (throw
            (ex-info "Public key error!" 
                     {:status 500})))))
 
(defn claims! [user authentication-system-user authentication-system settings]
  {:email (when (:send_email authentication-system) (:email user)) 
   :login (when (:send_login authentication-system) (:login user)) 
   :org_id (when (:send_org_id authentication-system) (:org_id user)) 
   :authentication_system_user_data (when (:send_auth_system_user_data authentication-system) 
                                      (:data authentication-system-user))
   :exp (time/plus (time/now) 
                   (if (= env/env :dev)
                     (time/hours 24)
                     (time/seconds 90)))
   :iat (time/now)
   :server_base_url (:external_base_url settings)
   :path (path :external-authentication-sign-in
               {:authentication-system-id (:id authentication-system)})})

(defn ext-auth-system-token-url
  [tx user-unique-id authentication-system-id settings]
  (let [data (authentication-system-user-data! user-unique-id authentication-system-id tx)
        authentication-system (-> data :authentication_system)
        priv-key (-> authentication-system :internal_private_key private-key!)
        claims (claims! (-> data :user)
                        (-> data :authentication_system_user) 
                        authentication-system settings)
        token (jwt/sign claims priv-key {:alg :es256})]
    (str (:external_url authentication-system) "?token=" token)))

(defn authentication-request 
  [{tx :tx :as request
    settings :settings
    {authentication-system-id :authentication-system-id} :route-params
    {user-unique-id :user-unique-id} :body}]
  (redirect (ext-auth-system-token-url tx
                                       user-unique-id
                                       authentication-system-id
                                       settings)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn authentication-system! [id tx]
  (or (->> (-> (sql/select :authentication_systems.*)
               (sql/from :authentication_systems)
               (sql/merge-where [:= :authentication_systems.id id])
               sql/format)
           (jdbc/query tx) first)
      (throw (ex-info "Authentication-System not found!" {:status 400}))))

(defn user-for-sign-in-token-query [sign-in-token authentication-system-id]
  (let [unique-ids [:email :login :org_id]
        unique-id (some sign-in-token unique-ids)
        query (auth-system-base-query-for-unique-id unique-id authentication-system-id)
        aggregator-fn (fn [query k]
                        (if-let [v (k sign-in-token)]
                          (sql/merge-where query [:= (keyword (str "users." k)) v])
                          query))
        reducer (partial reduce aggregator-fn) ]
    (-> (auth-system-base-query-for-unique-id unique-id authentication-system-id)
        (reducer unique-ids)
        (sql/merge-select :users.*)
        sql/format)))

(defn user-for-sign-in-token [sign-in-token authentication-system-id tx]
  (let [query (user-for-sign-in-token-query sign-in-token authentication-system-id)
        resultset (jdbc/query tx query)]
    (when (> (count resultset) 1)
      (throw (ex-info 
               "More than one user matched the sign-in request."
               {:status 400})))
    (or (first resultset) 
        (throw (ex-info 
                 "No valid user account could be identified for this sign-in request."
                 {:status 400})))))

(defn authentication-sign-in 
  [{{authentication-system-id :authentication-system-id} :route-params
    {token :token} :query-params-raw
    tx :tx
    :as request}]
  (let [authentication-system (authentication-system! authentication-system-id tx)
        external-pub-key (-> authentication-system :external_public_key public-key!)
        sign-in-token (jwt/unsign token external-pub-key {:alg :es256})
        internal-pub-key (-> authentication-system :internal_public_key public-key!)
        sign-in-request-token (jwt/unsign (:sign_in_request_token sign-in-token) 
                                          internal-pub-key {:alg :es256})]

    (logging/debug 'sign-in-token sign-in-token)
    (if-not (:success sign-in-token)
      {:status 400
       :body (:error-message sign-in-token)}
      (if-let [user (user-for-sign-in-token sign-in-token authentication-system-id tx)]
        (let [user-session (session/create-user-session user request)]
          {:body user
           :status 200
           :cookies {leihs.core.constants/USER_SESSION_COOKIE_NAME
                     {:value (:token user-session)
                      :http-only true
                      :max-age (* 10 356 24 60 60)
                      :path "/"
                      :secure (:sessions_force_secure (:settings request))}}})
        {:status 404}))))

(def routes
  (cpj/routes
    (cpj/POST (path :external-authentication-request 
                    {:authentication-system-id ":authentication-system-id"}) 
              [] #'authentication-request)
    (cpj/POST (path :external-authentication-sign-in
                    {:authentication-system-id ":authentication-system-id"}) 
              [] #'authentication-sign-in)))


;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
