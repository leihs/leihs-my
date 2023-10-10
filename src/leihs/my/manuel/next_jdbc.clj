(ns leihs.my.manuel.next_jdbc
  (:require [next.jdbc :as jdbc])
  (:import [com.zaxxer.hikari HikariConfig HikariDataSource])
  ;(:import [com.zaxxer.hikari HikariConfig HikariDataSource])
  )

;; TODO: Without db-connection-pool & without async request will be blocked
;(def db-spec {:dbtype   "postgresql"
;              :dbname   "leihs_dev"
;              :user     "leihs"
;              :password "leihs"
;              :host     "localhost"
;              :port     5415})
;
;(defn fetch-one-row "CAUTION: blocks if multiple requests are triggered, maybe async to solve this issue" []
;  (let [conn (jdbc/get-connection db-spec)
;        result (first (jdbc/execute! conn ["SELECT * FROM users LIMIT 1"]))]
;    (println "Fetched row: " result)
;    result))

(def datasource
  (let [config (doto (HikariConfig.)
                 (.setJdbcUrl "jdbc:postgresql://localhost:5415/leihs_dev")
                 (.setUsername "leihs")
                 (.setPassword "leihs")
                 (.setMaximumPoolSize 10))
        datasource (HikariDataSource. config)]
    datasource))

(def db-spec {:datasource datasource})

(defn fetch-one-row-from-pool "Use pool-connection" []
  (let [result (first (jdbc/execute! db-spec ["SELECT * FROM users LIMIT 1"]))]
    ;(println "Fetched row: " result)

    (def filtered-result (select-keys result [:users/lastname :users/firstname :users/city]))
    (println "Filtered result: " filtered-result)

    filtered-result))

(defn fetch-ten-row-from-pool "Use pool-connection" []
  (let [users (jdbc/execute! db-spec ["SELECT lastname, firstname, city, phone FROM users LIMIT 10"])]
    ;(println "Fetched row: " result)

    ; reduce dataset by keys
    (defn filter-maps [maps keys]
      (map #(select-keys % keys) maps))

    (def filtered-result (filter-maps users [:users/lastname :users/firstname :users/city]))
    (println "Filtered result: " filtered-result)

    filtered-result))

(defn -main [& _]
  (fetch-one-row-from-pool))
