(ns leihs.my.routes
  (:require
   [clojure.set]
   [leihs.core.anti-csrf.back :as anti-csrf]
   [leihs.core.auth.core :as auth]
   [leihs.core.auth.session]
   [leihs.core.db :as db]
   [leihs.core.http-cache-buster2 :as cache-buster :refer [wrap-resource]]
   [leihs.core.locale :as locale]
   [leihs.core.ring-audits :as ring-audits]
   [leihs.core.ring-exception :as ring-exception]
   [leihs.core.routes :as core-routes]
   [leihs.core.routing.back :as routing]
   [leihs.core.routing.dispatch-content-type :as dispatch-content-type]
   [leihs.core.settings :as settings]
   [leihs.core.status :as status]
   [leihs.my.auth-info.back :as auth-info]
   [leihs.my.authorization :as authorization]
   [leihs.my.back.html :as html]
   [leihs.my.initial-admin.back :as initial-admin]
   [leihs.my.password-restore.back :as password-restore]
   [leihs.my.paths :refer [path paths]]
   [leihs.my.resources.home.back :as home]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.cookies]
   [ring.middleware.json]
   [ring.middleware.params]
   [ring.util.response :refer [redirect]]))

(declare redirect-to-root-handler)

(def skip-authorization-handler-keys
  (clojure.set/union
   core-routes/skip-authorization-handler-keys
   #{:forgot-password
     :home
     :password-authentication
     :reset-password}))

; Handler keys not to be responded with "admin-style" SPA layout per ring middleware.
; Note that their route handlers still can respond with an SPA layout (see e.g. `leihs.my.resources.home.back`).
(def no-spa-handler-keys
  (clojure.set/union
   core-routes/no-spa-handler-keys
   #{:forgot-password
     :home
     :not-found
     :redirect-to-root
     :reset-password}))

(def resolve-table
  (merge core-routes/resolve-table
         {:auth-info auth-info/auth-info-handler
          :forgot-password password-restore/forgot-routes
          :home home/routes
          :not-found html/not-found-handler
          :redirect-to-root redirect-to-root-handler
          :reset-password password-restore/reset-routes}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn redirect-to-root-handler [request]
  (redirect (path :root)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn init []
  (routing/init paths resolve-table)
  ;(I> wrap-handler-with-logging
  (->
   routing/dispatch-to-handler
   (authorization/wrap skip-authorization-handler-keys)
   (dispatch-content-type/wrap-dispatch-html no-spa-handler-keys html/spa-handler)
   initial-admin/wrap
   ring-audits/wrap
   anti-csrf/wrap
   locale/wrap
   (auth/wrap-authenticate {:enable-auth-header-prefix-basic false})
   ring.middleware.cookies/wrap-cookies
   routing/wrap-empty
   settings/wrap
   db/wrap-tx
   (status/wrap (path :status))
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
              :cache-bust-paths ["/my/ui/my-ui.css"
                                 "my/ui/my-ui.css.map"
                                 "/my/js/main.js"]
              :never-expire-paths [#".*fontawesome-[^\/]*\d+\.\d+\.\d+\/.*"
                                   #".+_[0-9a-f]{40}\..+"]
              :cache-enabled? true})
   ring-exception/wrap))

;#### debug ###################################################################
;(debug/debug-ns *ns*)
