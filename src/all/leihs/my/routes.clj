(ns leihs.my.routes
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.core.anti-csrf.back :as anti-csrf]
    [leihs.core.auth.core :as auth]
    [leihs.core.ds :as ds]
    [leihs.core.http-cache-buster2 :as cache-buster :refer [wrap-resource]]
    [leihs.core.locale :as locale]
    [leihs.core.ring-audits :as ring-audits]
    [leihs.core.ring-exception :as ring-exception]
    [leihs.core.routes :as core-routes]
    [leihs.core.routing.back :as routing]
    [leihs.core.routing.dispatch-content-type :as dispatch-content-type]

    [leihs.my.authorization :as authorization]
    [leihs.my.back.html :as html]
    [leihs.my.constants :as constants]
    [leihs.my.env :as env]
    [leihs.my.initial-admin.back :as initial-admin]
    [leihs.my.language :as language]
    [leihs.my.paths :refer [path paths]]
    [leihs.my.password-restore :as password-restore]
    [leihs.my.resources.home.back :as home]
    [leihs.my.resources.settings.back :as settings]
    [leihs.my.resources.status.back :as status]
    [leihs.my.user.back :as user]
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

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug :refer [I>]]
    [logbug.ring :refer [wrap-handler-with-logging]]
    [logbug.thrown :as thrown]
    ))

(declare redirect-to-root-handler)

(def skip-authorization-handler-keys
  (clojure.set/union
    core-routes/skip-authorization-handler-keys
    #{:forgot-password
      :home
      :initial-admin
      :language
      :password-authentication
      :reset-password}))

(def no-spa-handler-keys
  (clojure.set/union
    core-routes/no-spa-handler-keys
    #{:forgot-password
      :home
      :language
      :not-found
      :redirect-to-root
      :reset-password
      }))

(def resolve-table
  (merge core-routes/resolve-table
         {:api-token api-token/routes
          :api-tokens api-tokens/routes
          :auth-info auth-info/ring-handler
          :forgot-password password-restore/forgot-routes
          :home home/routes
          :initial-admin initial-admin/routes
          :language language/routes
          :my-user user/routes
          :not-found html/not-found-handler
          :password password/routes
          :redirect-to-root redirect-to-root-handler
          :reset-password password-restore/reset-routes
          :status status/routes}))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn redirect-to-root-handler [request]
  (redirect (path :root)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn init []
  (routing/init paths resolve-table)
  (I> wrap-handler-with-logging
  ;(->
      routing/dispatch-to-handler
      (authorization/wrap skip-authorization-handler-keys)
      (dispatch-content-type/wrap-dispatch-html no-spa-handler-keys html/spa-handler)
      initial-admin/wrap
      ring-audits/wrap
      anti-csrf/wrap
      locale/wrap
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
                                     "/my/leihs-shared-bundle.js"
                                     "/my/js/app.js"]
                  :never-expire-paths [#".*fontawesome-[^\/]*\d+\.\d+\.\d+\/.*"
                                       #".+_[0-9a-f]{40}\..+"]
                  :cache-enabled? true})
      ring-exception/wrap))

;#### debug ###################################################################
(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
(debug/debug-ns *ns*)
