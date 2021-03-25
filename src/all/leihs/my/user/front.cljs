(ns leihs.my.user.front
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]])
  (:require
    [leihs.core.breadcrumbs :as breadcrumbs]
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.icons :as icons]
    [leihs.core.routing.front :as routing]
    [leihs.core.user.front :as user]
    [leihs.core.user.shared :refer [user-name-html]]

    [leihs.my.front.breadcrumbs :as my-breadcrumbs]
    [leihs.my.user.shared :refer [me?*]]
    [leihs.my.paths :as paths :refer [path]]
    [leihs.my.user.api-tokens.breadcrumbs :as api-tokens-breadcrumbs]
    [leihs.my.user.password.breadcrumbs :as password-breadcrumbs]))

(defn user-name-component []
  (let [user-data @user/state*
        user-id (:id user-data)]
    (user-name-html user-id user-data)))

(defn page []
  (let [user-id (if @me?*
                  (:id @user/state*)
                  (-> @routing/state* :route-params :user-id))]
    [:div.me
     (breadcrumbs/nav-component
       [(breadcrumbs/leihs-li)
        (my-breadcrumbs/user-li)]
       [(api-tokens-breadcrumbs/api-tokens-li)
        (password-breadcrumbs/password-li)
        (when (:is_admin @user/state*)
          (breadcrumbs/li (str "/admin/users/" user-id)
                          [:span icons/user-in-admin " User in the admin interface"]))])
     [:div
      (if-let [me (when @me?* @user/state*)]
        [:<>
         [:h1 "My user"]
         [:div.table-responsive
          [:table.table.table-borderless.table-sm
           [:tbody
            [:tr
             [:td.col-sm-2 "account UUID"]
             [:td [:span.text-monospace user-id]]]
            [:tr
             [:td.col-sm-2 "email adress"]
             [:td [:span.text-monospace (:email me)]]]]]]])]
     [:div]]))
