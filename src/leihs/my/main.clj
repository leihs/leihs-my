(ns leihs.my.main
  (:require [clj-yaml.core :as yaml]
            [clojure.pprint :refer [pprint]]
            [clojure.string]
            [clojure.tools.cli :as cli]
            [environ.core :refer [env]]
            [leihs.core.logging :as logging]
            [leihs.core.reload :as reload]
            [leihs.core.repl :as repl]
            [leihs.my.back.run :as run]
            [logbug.thrown :as thrown]
            [taoensso.timbre :refer [info]])
  (:gen-class))

(thrown/reset-ns-filter-regex #"^(leihs)\..*")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def cli-options
  (concat
   [["-h" "--help"]
    [nil "--dev-mode DEV_MODE" "dev mode"
     :default (or (some-> :dev-mode env yaml/parse-string) false)
     :parse-fn #(yaml/parse-string %)
     :validate [boolean? "Must parse to a boolean"]]]
   repl/cli-options))

(defn main-usage [options-summary & more]
  (->> ["Leihs Admin"
        ""
        "usage: leihs-admin [<opts>] SCOPE|COMMAND [<scope-opts>] [<args>]"
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

(defn main [args]
  (logging/init)
  (let [{:keys [options arguments errors summary]}
        (cli/parse-opts args cli-options :in-order true)
        pass-on-args (->> [options (rest arguments)]
                          flatten (into []))
        cmd (some-> arguments first keyword)
        options (into (sorted-map) options)
        print-summary #(println (main-usage summary {:args args :options options}))]
    (info *ns* {'args args 'options options 'cmd cmd})
    (repl/init options)
    (cond
      (:help options) (print-summary)
      :else (case cmd
              :run (run/main options (rest arguments))
              (print-summary)))))

(defn -main [& args]
  (reset! reload/args* (or args []))
  (main args))

; dynamic restart on require
(when @reload/args* (main @reload/args*))
