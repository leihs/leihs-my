(ns leihs.my.user.auth-info.back
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.core :refer [keyword str presence]]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [logbug.catcher :as catcher]
    ))

(defn ring-handler [request]
  (if-let [auth-ent (:authenticated-entity request)]
    {:status 200
     :body auth-ent}))
