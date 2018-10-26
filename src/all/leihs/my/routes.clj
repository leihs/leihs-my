(ns leihs.my.routes
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.core.anti-csrf.back :as anti-csrf]
    [leihs.core.ds :as ds]
    [leihs.core.http-cache-buster :as cache-buster :refer [wrap-resource]]
    [leihs.core.ring-exception :as ring-exception]
    [leihs.core.routing.back :as routing]
    [leihs.core.routing.dispatch-content-type :as dispatch-content-type]
    [leihs.core.auth.core :as auth]
    [leihs.core.shutdown :as shutdown]
    [leihs.core.remote-navbar.back :as remote-navbar]

    [leihs.my.authorization :as authorization]
    [leihs.my.back.html :as html]
    [leihs.my.constants :as constants]
    [leihs.my.env :as env]
    [leihs.my.initial-admin.back :as initial-admin]
    [leihs.my.paths :refer [path paths]]
    [leihs.my.resources.settings.back :as settings]
    [leihs.my.resources.status.back :as status]
    [leihs.my.sign-in.back :as sign-in]
    [leihs.my.sign-in.external-authentication.back :as external-authentication]
    [leihs.my.sign-in.password-authentication.back :as password-authentication]
    [leihs.my.sign-out.back :as sign-out]
    [leihs.my.user.api-token.back :as api-token]
    [leihs.my.user.api-tokens.back :as api-tokens]
    [leihs.my.user.auth-info.back :as auth-info]
    [leihs.my.user.password.back :as password]

    [compojure.core :as cpj]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.cookies]
    [ring.middleware.json]
    [ring.middleware.params]
    [ring.util.response :refer [redirect]]

    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug :refer [I>]]
    [logbug.ring :refer [wrap-handler-with-logging]]
    [logbug.thrown :as thrown]
    ))

(declare redirect-to-root-handler)

(def skip-authorization-handler-keys
  #{:external-authentication-request
    :external-authentication-sign-in
    :initial-admin
    :password-authentication
    :shutdown
    :sign-in
    :sign-out})

(def no-html-handler-keys
  #{:redirect-to-root
    :navbar
    :not-found})

(def resolve-table
  {:api-token api-token/routes
   :api-tokens api-tokens/routes
   :auth-info auth-info/ring-handler
   :initial-admin initial-admin/routes
   :navbar remote-navbar/handler
   :not-found html/not-found-handler
   :password-authentication password-authentication/routes
   :password leihs.my.user.password.back/routes
   :external-authentication-request external-authentication/routes
   :external-authentication-sign-in external-authentication/routes
   :redirect-to-root redirect-to-root-handler
   :shutdown shutdown/ring-handler
   :sign-in sign-in/routes
   :sign-out sign-out/routes
   :status status/routes

   ;   :auth-info auth/routes
   ;   :auth-password-sign-in auth/routes
   ;   :auth-shib-sign-in auth/routes
   ;   :sign-out auth/routes
   })



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn redirect-to-root-handler [request]
  (redirect (path :root)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn init []
  (routing/init paths resolve-table)
  (I> wrap-handler-with-logging
      routing/dispatch-to-handler
      (authorization/wrap skip-authorization-handler-keys)
      (dispatch-content-type/wrap-dispatch-html no-html-handler-keys html/html-handler)
      initial-admin/wrap
      anti-csrf/wrap
      auth/wrap-authenticate
      ring.middleware.cookies/wrap-cookies
      routing/wrap-empty
      settings/wrap
      ds/wrap-tx
      status/wrap
      ring.middleware.json/wrap-json-response
      (ring.middleware.json/wrap-json-body {:keywords? true})
      dispatch-content-type/wrap-accept
      routing/wrap-add-vary-header
      routing/wrap-resolve-handler
      routing/wrap-canonicalize-params-maps
      ring.middleware.params/wrap-params
      wrap-content-type
      (wrap-resource
        "public" {:allow-symlinks? true
                  :cache-bust-paths ["/my/css/site.css"
                                     "/my/css/site.min.css"
                                     "/my/js/app.js"]
                  :no-expire-paths [#".*font-awesome-[^\/]*\d\.\d\.\d\/.*"
                                       #".+_[0-9a-f]{40}\..+"]
                  :enabled? (= env/env :prod)})
      ring-exception/wrap))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
