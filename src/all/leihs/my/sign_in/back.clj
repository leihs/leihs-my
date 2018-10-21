(ns leihs.my.sign-in.back
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.sql :as sql]
    [leihs.my.paths :refer [path]]
    [leihs.my.sign-in.shared :refer [auth-system-base-query-for-uniqe-id]]

    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect]]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]))

(defn auth-system-query [email]
  (-> email
      auth-system-base-query-for-uniqe-id
      (sql/merge-select
        :authentication_systems.id
        :authentication_systems.type
        :authentication_systems.name
        :authentication_systems.description
        :authentication_systems.external_url)
      sql/format))

;(-> "admin@exmaple.com" auth-system-query)

(defn sign-in [{tx :tx {email :email} :query-params}]
  {:body (->> email
              auth-system-query
              (jdbc/query tx))})

(def routes
  (cpj/routes
    (cpj/GET (path :sign-in) [] #'sign-in)))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
