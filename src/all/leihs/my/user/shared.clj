(ns leihs.my.user.shared
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.sql :as sql]

    [clojure.java.jdbc :as jdbc]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [logbug.catcher :as catcher]
    ))

                
(defn wrap-me-id 
  ([handler]
   (fn [request]
     (wrap-me-id handler request)))
  ([handler request]
   (handler
     (if (= "me" (-> request :route-params :user-id))
       (assoc-in request [:route-params :user-id]
                 (-> request :authenticated-entity :user_id))
       request))))


(defn password-hash
  ([password tx]
   (->> ["SELECT crypt(?,gen_salt('bf',10)) AS pw_hash" password]
        (jdbc/query tx)
        first :pw_hash)))

(defn sql-command [user-id pw-hash]
  (-> (sql/insert-into :authentication_systems_users)
      (sql/values [{:user_id user-id
                    :authentication_system_id "password"
                    :data pw-hash}])
      (sql/upsert (-> (sql/on-conflict :user_id :authentication_system_id)
                      (sql/do-update-set :data)
                      ((fn [sql]
                         (apply sql/do-update-set sql [:data])
                         ))))
      (sql/returning :*)
      sql/format))


(defn set-password [user-id password tx]
  (let [pw-hash (password-hash password tx)
        sql-command (sql-command user-id pw-hash)
        result (jdbc/execute! tx sql-command {:return-keys true})]
    {:body result}))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
