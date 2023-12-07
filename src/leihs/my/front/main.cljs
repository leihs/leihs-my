(ns leihs.my.front.main
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [leihs.core.core :refer [keyword str presence]]
   [leihs.core.global :as global]
   [leihs.core.user.front :refer [load-user-data-from-dom]]

   [leihs.my.front.html :as html]
   [leihs.my.paths]
   [leihs.my.routes :as routes]))

(defn init! []
  (load-user-data-from-dom)
  (global/init)
  (routes/init)
  (html/mount))
