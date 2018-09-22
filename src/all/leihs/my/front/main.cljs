(ns leihs.my.front.main
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.global :as global]

    [leihs.my.paths]
    [leihs.my.front.html :as html]
    [leihs.my.routes :as routes]))

(defn init! []
  (global/init)
  (routes/init)
  (html/mount))
