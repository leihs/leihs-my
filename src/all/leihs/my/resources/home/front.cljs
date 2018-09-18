(ns leihs.my.resources.home.front
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.user.front :as user]

    [leihs.my.front.breadcrumbs :as breadcrumbs]

    [accountant.core :as accountant]
    [cljs.core.async :as async]
    [cljs.core.async :refer [timeout]]
    [cljs.pprint :refer [pprint]]
    [reagent.core :as reagent]
    ))

(defn page []
  [:div.home
   (when-let [user @user/state*]
     (breadcrumbs/nav-component
       [(breadcrumbs/leihs-li)]
       [(breadcrumbs/admin-li)
        (breadcrumbs/borrow-li)
        (breadcrumbs/lending-li)
        (breadcrumbs/procurement-li)]))

   [:h1 "leihs - Equipment Booking and Inventory Management System"]
   [:p  "Manage inventory, place reservations on items and pick them up."]])
