(ns leihs.my.user.password.back
  (:refer-clojure :exclude [keyword str])
  (:require
   [compojure.core :as cpj]
   [crypto.random]
   [leihs.my.paths :refer [path]]
   [leihs.my.user.shared :refer [set-password wrap-me-id]]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]))

(defn reset-password-ring-handler
  [{tx :tx-next
    {user-id :user-id} :route-params
    {password :password} :body}]
  (assert (set-password user-id password tx))
  {:status 204})

(def password-path
  (path :password {:user-id ":user-id"}))

(def routes
  (cpj/routes
   (cpj/PUT password-path [] (-> reset-password-ring-handler wrap-me-id))))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
