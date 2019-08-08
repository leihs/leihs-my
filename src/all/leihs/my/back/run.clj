(ns leihs.my.back.run
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.core.ds :as ds]
    [leihs.core.http-server :as http-server]
    [leihs.core.shutdown :as shutdown]
    [leihs.core.url.http :as http-url]
    [leihs.core.url.jdbc :as jdbc-url]
    [leihs.core.url.jdbc]

    [leihs.my.env]
    [leihs.my.paths]
    [leihs.my.resources.status.back :as status]
    [leihs.my.routes :as routes]

    [clojure.core.async :as async]
    [clojure.tools.cli :as cli :refer [parse-opts]]
    [clojure.pprint :refer [pprint]]

    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    ))

(def defaults
  {:LEIHS_MY_HTTP_BASE_URL "http://localhost:3240"
   :LEIHS_DATABASE_URL "jdbc:postgresql://leihs:leihs@localhost:5432/leihs?min-pool-size=1&max-pool-size=16"
   })

(defn run [options]
  (catcher/snatch
    {:return-fn (fn [e] (System/exit -1))}
    ; ---------------------------------------------------
    ; provide implementation fo render-page-base function
    (require 'leihs.my.back.ssr)
    ; ---------------------------------------------------
    (logging/info "Invoking run with options: " options)
    (shutdown/init options)
    (let [status (status/init)
          ds (ds/init (:database-url options) (:health-check-registry status))
          app-handler (routes/init)
          http-server (http-server/start (:http-base-url options) app-handler)])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn env-or-default [kw]
  (or (-> (System/getenv) (get (str kw) nil) presence)
      (get defaults kw nil)))

(defn extend-pg-params [params]
  (assoc params
         :password (or (:password params)
                       (System/getenv "PGPASSWORD"))
         :username (or (:username params)
                       (System/getenv "PGUSER"))
         :port (or (:port params)
                   (System/getenv "PGPORT"))))

(def cli-options
  [["-h" "--help"]
   ["-b" "--http-base-url LEIHS_MY_HTTP_BASE_URL"
    (str "default: " (:LEIHS_MY_HTTP_BASE_URL defaults))
    :default (http-url/parse-base-url (env-or-default :LEIHS_MY_HTTP_BASE_URL))
    :parse-fn http-url/parse-base-url]
   ["-d" "--database-url LEIHS_DATABASE_URL"
    (str "default: " (:LEIHS_DATABASE_URL defaults))
    :default (-> (env-or-default :LEIHS_DATABASE_URL)
                 jdbc-url/dissect extend-pg-params)
    :parse-fn #(-> % jdbc-url/dissect extend-pg-params)]
   shutdown/pid-file-option])

(defn main-usage [options-summary & more]
  (->> ["Leihs PERM run "
        ""
        "usage: leihs-perm run [<opts>] [<args>]"
        ""
        "Options:"
        options-summary
        ""
        ""
        (when more
          ["-------------------------------------------------------------------"
           (with-out-str (pprint more))
           "-------------------------------------------------------------------"])]
       flatten (clojure.string/join \newline)))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]}
        (cli/parse-opts args cli-options :in-order true)
        pass-on-args (->> [options (rest arguments)]
                          flatten (into []))]
    (cond
      (:help options) (println (main-usage summary {:args args :options options}))
      :else (run options))))

;(-main "-h")
;(-main)


