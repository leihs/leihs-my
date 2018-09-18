(ns leihs.my.resources.settings.back
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.my.paths :refer [path]]
    [leihs.core.sql :as sql]
    [leihs.shared :refer [password-hash]]

    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect]]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug])
  (:import
    [java.util UUID]
    ))


(defn settings! [tx]
  (or (->> ["SELECT * FROM settings"] (jdbc/query tx) first)
      (first (jdbc/insert! tx :settings {:id 0}))
      (throw (IllegalStateException. "No settings here!"))))


(defn wrap
  ([handler]
   (fn [request]
     (wrap handler request)))
  ([handler request]
   (handler (assoc request :settings (settings! (:tx request))))))


;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)
