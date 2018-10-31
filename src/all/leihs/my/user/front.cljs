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

    [leihs.my.front.breadcrumbs :as my-breadcrumbs]
    [leihs.my.user.shared :refer [me?*]]
    [leihs.my.paths :as paths :refer [path]]
    [leihs.my.user.api-tokens.breadcrumbs :as api-tokens-breadcrumbs]
    [leihs.my.user.password.breadcrumbs :as password-breadcrumbs]))



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
      (if @me?*
        [:h1 "My leihs Home"]
        (let [id (-> @routing/state* :route-params :user-id)]
          [:div 
           [:h1 "User's Home"]
           [:p "user-id: " [:code id ]]]))]
     [:div
      [:p "There will be some awesome information in the future here."]]]))
