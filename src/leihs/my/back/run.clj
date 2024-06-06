(ns leihs.my.back.run
  (:require
   [clojure.pprint :refer [pprint]]
   [clojure.tools.cli :as cli]
   [leihs.core.db :as db]
   [leihs.core.http-server :as http-server]
   [leihs.core.shutdown :as shutdown]
   [leihs.core.sign-in.back :refer [use-sign-in-page-renderer]]
   [leihs.core.status :as status]
   [leihs.my.back.html :refer [auth-page]]
   [leihs.my.paths]
   [leihs.my.routes :as routes]
   [logbug.catcher :as catcher]
   [taoensso.timbre :refer [info]]))

(defn run [options]
  (catcher/snatch
   {:return-fn (fn [e] (System/exit -1))}
   (info "Invoking run with options: " options)
   (shutdown/init options)
   (use-sign-in-page-renderer auth-page)
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
