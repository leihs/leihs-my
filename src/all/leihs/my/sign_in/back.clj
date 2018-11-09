(ns leihs.my.sign-in.back
  (:refer-clojure :exclude [str keyword])
  (:require [clojure.tools.logging :as log]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [compojure.core :as cpj]
            [leihs.core.auth.session :as session]
            [leihs.core.password-authentication.back :refer [password-check-query]]
            [leihs.core.sql :as sql]
            [leihs.my.back.ssr :as ssr]
            [leihs.my.paths :refer [path]]
            [leihs.my.sign-in.shared :refer [auth-system-base-query-for-unique-id]]
            [leihs.my.sign-in.external-authentication.back
             :refer [ext-auth-system-token-url]]
            [leihs.my.utils.redirects :refer [redirect-target]]
            [ring.util.response :refer [redirect]]))

(defn auth-system-query
  [unique-id]
  (-> unique-id
      auth-system-base-query-for-unique-id
      (sql/merge-select :authentication_systems.id
                        :authentication_systems.type
                          :authentication_systems.name
                        :authentication_systems.description
                          :authentication_systems.external_url)
      sql/format))

(defn auth-systems
  [tx unique-id]
  (->> unique-id
       auth-system-query
       (jdbc/query tx)))

(def sign-in-error-flash
  {:level "error",
   :message (clojure.string/join
              " \n"
              ["Signing in with this account is currently not possible! "
               "Check your email-address respectively login and try again. "
               "Contact your leihs administrator if the problem persists. "])})

(defn sign-in-get
  [{tx :tx,
    settings :settings,
    {user-param :user} :query-params,
    :as request}]
  (if-let [user (:authenticated-entity request)]
    (redirect (redirect-target tx user))
    (let [user-auth-systems (auth-systems tx user-param)]
      (if (empty? user-auth-systems)
        (ssr/render-sign-in-page user-param
                                 request
                                 {:flashMessages [sign-in-error-flash]})
        (let [user-auth-systems-props {:authSystems user-auth-systems}
              render-sign-in-page-fn #(ssr/render-sign-in-page user-param
                                                               request
                                                               user-auth-systems-props)]
          (if (= (count user-auth-systems) 1)
            (let [auth-system (first user-auth-systems)]
              (if (= (:type auth-system) "external")
                (redirect (ext-auth-system-token-url tx
                                                     user-param
                                                     (:id auth-system)
                                                     settings))
                (render-sign-in-page-fn)))
            (render-sign-in-page-fn)))))))

(defn sign-in-post
  [{tx :tx,
    {user-param :user, password :password} :form-params,
    settings :settings,
    :as request}]

  (if-let [user (->> [user-param password]
                     (apply password-check-query)
                     (jdbc/query tx)
                     first)]
    (let [user-session (session/create-user-session user request)]
      {:status 302,
       :headers {"Location" (redirect-target tx user)}
       :cookies {leihs.core.constants/USER_SESSION_COOKIE_NAME
                   {:value (:token user-session),
                    :http-only true,
                    :max-age (* 10 356 24 60 60),
                    :path "/",
                    :secure (:sessions_force_secure settings)}}})
    {:status 401,
     :headers {"Content-Type" "text/html"},
     :body
       (ssr/render-sign-in-page
         user-param
         request
         {:flashMessages [
           {:level "error", :message
             (clojure.string/join
               " \n"
               ["Password authentication failed!"
                "Check your password and try again."
                "Contact your leihs administrator if the problem persists."])}]})}))



(def routes
  (cpj/routes (cpj/GET (path :sign-in) [] #'sign-in-get)
              (cpj/POST (path :sign-in) [] #'sign-in-post)))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
