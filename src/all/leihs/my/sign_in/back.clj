(ns leihs.my.sign-in.back
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.my.paths :refer [path]]
    [leihs.core.sql :as sql]
    [leihs.core.sql :as sql]

    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect]]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]))



(defn auth-system-query [email]
  (-> (sql/select :authentication_systems.id
                  :authentication_systems.type
                  :authentication_systems.name
                  :authentication_systems.description
                  :authentication_systems.external_base_url)
      (sql/from :authentication_systems)
      (sql/merge-where [:= :authentication_systems.enabled true])
      (sql/merge-join :authentication_systems_users
                      [:= :authentication_systems_users.authentication_system_id
                       :authentication_systems.id])
      (sql/merge-join :users
                      [:= :users.id
                       :authentication_systems_users.user_id])
      (sql/merge-join [:= :users.account_enabled true])
      (sql/merge-where [:= :users.email (-> email (or "") str/lower-case)])
      sql/format))

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
