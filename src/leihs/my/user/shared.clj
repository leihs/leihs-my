(ns leihs.my.user.shared
  (:refer-clojure :exclude [keyword str])
  (:require
    [honey.sql :refer [format] :rename {format sql-format}]
    [honey.sql.helpers :as sql]
    [leihs.core.constants :refer [PASSWORD_AUTHENTICATION_SYSTEM_ID]]
    [next.jdbc :as jdbc]))

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
   (-> (sql/select [[:crypt password [:gen_salt "bf"]] :pw_hash])
       sql-format
       (->> (jdbc/execute-one! tx))
       :pw_hash)))

(defn sql-command [user-id pw-hash]
  (-> (sql/insert-into :authentication_systems_users)
      (sql/values [{:user_id [:cast user-id :uuid]
                    :authentication_system_id PASSWORD_AUTHENTICATION_SYSTEM_ID
                    :data pw-hash}])
      (sql/on-conflict :user_id :authentication_system_id)
      (sql/do-update-set :data {:raw "EXCLUDED.data"})
      (sql/returning :*)
      sql-format))

(defn set-password [user-id password tx]
  (let [pw-hash (password-hash password tx)
        sql-command (sql-command user-id pw-hash)
        result (jdbc/execute! tx sql-command {:return-keys true})]
    {:body result}))

;#### debug ###################################################################
;(debug/debug-ns *ns*)
