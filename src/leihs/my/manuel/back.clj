(ns leihs.my.manuel.back
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.sql :as sql]

    [leihs.my.paths :refer [path]]
    [leihs.my.user.shared :refer [set-password]]

    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect]]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]))

(defn fetch-first-user [tx]
  (-> (sql/select :*)
      (sql/from :users)
      (sql/limit 1)
      sql/format
      (->> (jdbc/query tx))
      first))

(defn fetch-first-user-next [tx-next]
  {:user "user"})

(defn manuel-get [request]
  (let [tx (:tx request)]
    {:status 200
     :headers {"Content-Type" "text/html"},
     :body (fetch-first-user tx)}))

(def routes
  (cpj/routes
    (cpj/GET (path :manuel) [] manuel-get)))
