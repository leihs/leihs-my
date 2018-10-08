(ns leihs.my.sign-in.shared
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.my.paths :refer [path]]
    [leihs.core.sql :as sql]

    [clojure.string :as str]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]))

(def auth-system-user-base-query
  (-> (sql/from :authentication_systems)
      (sql/merge-where [:= :authentication_systems.enabled true])
      (sql/merge-join :authentication_systems_users
                      [:= :authentication_systems_users.authentication_system_id
                       :authentication_systems.id])
      (sql/merge-join :users
                      [:= :users.id
                       :authentication_systems_users.user_id])
      (sql/merge-join [:= :users.account_enabled true])
      (sql/order-by [:authentication_systems.priority :desc] :authentication_systems.id)))


(defn auth-system-base-query-for-uniqe-id 
  ([unique-id]
   (-> auth-system-user-base-query 
       (sql/merge-where 
         [:or 
          [:= :users.org_id unique-id]
          [:= :users.login unique-id]
          [:= (sql/raw "lower(users.email)") (-> unique-id (or "") str/lower-case)]])))
  ([user-unique-id authentication-system-id]
   (-> (auth-system-base-query-for-uniqe-id user-unique-id)
       (sql/merge-where [:= :authentication_systems.id authentication-system-id]))))
