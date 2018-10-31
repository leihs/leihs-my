(ns leihs.my.user.shared
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]])

  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.routing.front :as routing]
    ))

(def me?*
  (reaction
    (= "me" (-> @routing/state* :route-params :user-id))))
