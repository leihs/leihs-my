(ns leihs.my.user.api-tokens.back
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.core.sql :as sql]

    [leihs.my.paths :refer [path]]
    [leihs.my.user.api-token.back :as api-token :refer [wrap-me-id]]

    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))

(defn api-tokens-query [user-id]
  (-> (sql/select :id :token_part :scope_read :scope_write :scope_admin_read :scope_admin_write :expires_at :created_at)
      (sql/from :api_tokens)
      (sql/merge-where [:= :api_tokens.user_id user-id])
      (sql/order-by [:created_at :desc])
      sql/format))

(defn api-tokens
  ([{tx :tx {user-id :user-id} :route-params}]
   (api-tokens user-id tx))
  ([user-id tx]
   {:body
    {:api-tokens
     (jdbc/query tx (api-tokens-query user-id))}}))



(def routes
  (cpj/routes
    (cpj/GET (path :api-tokens {:user-id ":user-id"}) 
             [] (-> #'api-tokens wrap-me-id))
    (cpj/POST (path :api-tokens {:user-id ":user-id"}) 
              [] (-> #'api-token/routes wrap-me-id))))


;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)
