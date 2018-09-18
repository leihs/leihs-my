(ns leihs.my.front.init
  (:require [leihs.my.front.main]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(leihs.my.front.main/init!)
