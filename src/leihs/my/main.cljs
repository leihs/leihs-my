(ns leihs.my.main
  (:require
   [leihs.core.logging]
   [leihs.my.front.main]
   [taoensso.timbre :refer [info]]))

(defn ^:dev/after-load init [& args]
  (leihs.core.logging/init)
  (info  "initializing" 'leihs.my.main)
  (leihs.my.front.main/init!))
