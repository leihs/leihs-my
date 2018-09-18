(ns leihs.my.resources.shutdown.back
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.my.paths :refer [path]]
    [leihs.core.sql :as sql]

    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect]]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug])
  (:import
    [java.util UUID]
    ))

(def enabled* (atom false))

(defn- shutdown [request]
  (if @enabled*
    (do (future (Thread/sleep 500)
                (System/exit 0))
        {:status 204
         :body "shutting down in 500 ms"})
    {:staus 403}))

(def routes
  (cpj/routes
    (cpj/POST (path :shutdown) [] #'shutdown)))

(defn init [options]
  (when (:enable-shutdown-route options)
    (reset! enabled* true)))
