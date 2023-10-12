(ns leihs.my.user.api-tokens.back
  (:refer-clojure :exclude [keyword str])
  (:require
    [compojure.core :as cpj]
    [honey.sql :refer [format] :rename {format sql-format}]
    [leihs.core.sql :as sql]
    [leihs.my.paths :refer [path]]
    [leihs.my.user.api-token.back :as api-token]
    [leihs.my.user.shared :refer [wrap-me-id]]
    [next.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]))

(defn api-tokens-query [user-id]
  (-> (sql/select :id :token_part :scope_read :scope_write :scope_admin_read :scope_admin_write :expires_at :created_at)
      (sql/from :api_tokens)
      (sql/where [:= :api_tokens.user_id [:cast user-id :uuid]])
      (sql/order-by [:created_at :desc])
      sql-format))

(defn api-tokens
  ([{tx :tx-next {user-id :user-id} :route-params}]
   (api-tokens user-id tx))
  ([user-id tx]
   {:body
    {:api-tokens
     (jdbc/execute! tx (api-tokens-query user-id))}}))

(def routes
  (cpj/routes
    (cpj/GET (path :api-tokens {:user-id ":user-id"})
             [] (-> #'api-tokens wrap-me-id))
    (cpj/POST (path :api-tokens {:user-id ":user-id"})
              [] (-> #'api-token/routes wrap-me-id))))


;#### debug ###################################################################
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)
