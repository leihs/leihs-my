(ns leihs.my.manuel.back
    (:refer-clojure :exclude [keyword str])
    (:require
      [clojure.java.jdbc :as jdbc]
      [clojure.tools.logging :as logging]

      [compojure.core :as cpj]
      [leihs.core.core :refer [keyword presence str]]

      [leihs.core.sql :as sql]
      [leihs.my.paths :refer [path]]

      ;TODO: askMatus
      ;[logbug.debug :as debug]
      [taoensso.timbre :refer [debug info warn error spy]]

      [ring.util.response :refer [redirect]]))

(defn extract-as-csv "extract a couple of attr to csv-format" [e]
      (let [
            firstname (:firstname e)
            lastname (:lastname e)
            city (:city e)
            zip (:zip e)
            phone (:phone e)
            title "firstname;lastname;city;zip;phone\n"
            joined (clojure.string/join ";" [firstname lastname city zip phone])
            result (str title joined)
            ]

           (debug  (str ">> myDebug 01: " result))
           (println  (str ">> myDebug 02: " result))

           result))



(defn fetch-first-user-by-mode "extract first user-entry"

      ([tx mode] (let [entry (fetch-first-user-by-mode tx)]
                      (println "fetch-first-user::mode" mode)
                      (cond (= mode "csv") (extract-as-csv entry)
                            :else entry
                            )))

      ([tx]
       (-> (sql/select :*)
           (sql/from :users)
           (sql/limit 1)
           sql/format
           (->> (jdbc/query tx))
           first)))

(defn fetch-first-user-next [tx-next]
      {:user "user"})

(defn manuel-get "extract 'mode=<json|csv|stream>'  default: text" [request]
      (println request)
      (println "query-params: " (:query-params request))
      (println "mode:" (:mode (:query-params request)))


      (let [tx (:tx request)
            query-params (:query-params request)
            mode-raw (:mode query-params)
            mode (cond (= mode-raw nil) "text/html"
                       (= mode-raw "json") "application/json"
                       (= mode-raw "csv") "text/csv"
                       (= mode-raw "stream") "application/octet-stream"
                       :else "text/html"
                       )]

           {:status  200
            :headers {"Content-Type" mode},
            :body    (fetch-first-user-by-mode tx mode-raw)}
           ))

(def routes
  (cpj/routes
    (cpj/GET (path :manuel) [] manuel-get)))
