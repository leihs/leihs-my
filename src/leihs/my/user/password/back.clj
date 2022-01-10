(ns leihs.my.user.password.back
  (:refer-clojure :exclude [str keyword])
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.set :refer [rename-keys]]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [crypto.random]
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.sql :as sql]
    [leihs.my.paths :refer [path]]
    [leihs.my.user.shared :refer [wrap-me-id set-password]]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    ))



(defn reset-password-ring-handler
  [{tx :tx
    {user-id :user-id} :route-params
    {password :password} :body }]
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
