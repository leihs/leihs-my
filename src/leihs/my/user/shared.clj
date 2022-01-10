(ns leihs.my.user.shared
  (:refer-clojure :exclude [str keyword])
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [leihs.core.auth.shared :refer [password-hash]]
    [leihs.core.constants :refer [PASSWORD_AUTHENTICATION_SYSTEM_ID]]
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.sql :as sql]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
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

(defn sql-command [user-id pw-hash]
  (-> (sql/insert-into :authentication_systems_users)
      (sql/values [{:user_id user-id
                    :authentication_system_id PASSWORD_AUTHENTICATION_SYSTEM_ID
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
;(debug/debug-ns *ns*)
