(ns leihs.my.back.run
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [clojure.core.async :as async]
    [clojure.pprint :refer [pprint]]
    [clojure.tools.cli :as cli :refer [parse-opts]]
    [environ.core :refer [env]]
    [leihs.core.db :as db]
    [leihs.core.http-server :as http-server]
    [leihs.core.shutdown :as shutdown]
    [leihs.core.ssr-engine :as ssr-engine]
    [leihs.my.back.ssr]
    [leihs.core.status :as status]
    [leihs.core.url.jdbc :as jdbc-url]
    [leihs.core.url.jdbc]
    [leihs.my.paths]
    [leihs.my.routes :as routes]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    [taoensso.timbre :refer [debug info warn error]]
    ))

(defn run [options]
  (catcher/snatch
    {:return-fn (fn [e] (System/exit -1))}
    (info "Invoking run with options: " options)
    (shutdown/init options)
    (ssr-engine/init options)
    (leihs.core.ssr/init leihs.my.back.ssr/render-page-base)
    (let [status (status/init)]
      (db/init options (:health-check-registry status)))
    (let [http-handler (routes/init)]
      (http-server/start options http-handler))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def cli-options
  (concat
    [["-h" "--help"]
     shutdown/pid-file-option]
    db/cli-options
    (http-server/cli-options :default-http-port 3240)))

(defn main-usage [options-summary & more]
  (->> ["leihs-my"
        ""
        "usage: leihs-my [<opts>] [<args>]"
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

(defn main [gopts args]
  (let [{:keys [options arguments errors summary]}
        (cli/parse-opts args cli-options :in-order true)
        pass-on-args (->> [options (rest arguments)]
                          flatten (into []))
        options (merge gopts options)]
    (info *ns* 'main {'args args 'options options})
    (cond
      (:help options) (println (main-usage summary {:args args :options options}))
      :else (run options))))
