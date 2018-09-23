(ns leihs.my.user.password.breadcrumbs
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.icons :as icons]
    [leihs.core.routing.front :as routing]
    [leihs.core.breadcrumbs :as breadcrumbs :refer [li]]))


(def route-params*
  (reaction
    {:user-id (-> @routing/state* :route-params :user-id)}))

(defn password-li []
  (li :password [:span icons/password " Password "] 
      @route-params* {}))

