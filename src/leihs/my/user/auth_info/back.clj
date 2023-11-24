(ns leihs.my.user.auth-info.back
  (:refer-clojure :exclude [str keyword])
  (:require
   [clojure.tools.logging :as logging]
   [leihs.core.core :refer [keyword str presence]]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]))

(defn ring-handler [request]
  (if-let [auth-ent (:authenticated-entity request)]
    {:status 200
     :body auth-ent}))
