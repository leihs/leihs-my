(ns leihs.my.user.password.back
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.sql :as sql]

    [leihs.my.paths :refer [path]]
    [leihs.my.user.shared :refer [wrap-me-id set-password]]

    [clojure.set :refer [rename-keys]]
    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [crypto.random]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [logbug.catcher :as catcher]
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
