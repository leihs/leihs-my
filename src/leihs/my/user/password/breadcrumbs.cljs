(ns leihs.my.user.password.breadcrumbs
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [leihs.core.auth.core :as auth]
   [leihs.core.breadcrumbs :as breadcrumbs :refer [li]]
   [leihs.core.core :refer [keyword str presence]]
   [leihs.core.routing.front :as routing]
   [leihs.my.front.icons :as icons]))

(def route-params*
  (reaction
   {:user-id (or (-> @routing/state* :route-params :user-id)
                 ":user-id")}))

(defn password-li []
  (li :password [:span [icons/password] " Password "]
      @route-params* {}
      :authorizers [auth/all-granted]))

