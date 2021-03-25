(ns leihs.my.user.front
  (:refer-clojure :exclude [str keyword])
  #_(:require-macros
    [reagent.ratom :as ratom :refer [reaction]])
  (:require
    [leihs.core.breadcrumbs :as breadcrumbs]
    [leihs.core.core :refer [str]]
    [leihs.core.icons :as icons]
    [leihs.core.routing.front :as routing]
    [leihs.core.user.front :as user]
    [leihs.core.user.shared :refer [user-name-html]]

    [leihs.my.front.breadcrumbs :as my-breadcrumbs]
    [leihs.my.user.shared :refer [me?*]]
    #_[leihs.my.paths :as paths :refer [path]]
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
     
     [:div.vh-100 ; note: push down the footer
     
      ;; looking at my own user
      (if-let [me (when @me?* @user/state*)]
        [:<>
         [:h1 "My user"]

         (when (empty? (me :access-rights))
           [:div.ui-no-access-rights-warning.py-2.my-2
            [:div.alert.alert-warning {:role "alert"}
             [:div.row.text-center
              [:div.col-sm-1.d-flex.mt-2.mb-3.m-sm-0 [:span.m-auto [:i.fas.fa-exclamation-triangle]]]
              [:div.col-sm.text-center
               [:p "You can not use the borrow section because you dont have access rights to any inventory pool!"]
               [:p.mb-0 "Please contact your support or lending desk."]]
              [:div.col-sm-1.d-flex.mt-3.mb-2.m-sm-0 [:span.m-auto [:i.fas.fa-exclamation-triangle]]]]]])

         [:div.ui-user-info-table.table-responsive
          [:table.table.table-borderless.table-sm
           [:tbody
            [:tr
             [:td.col-sm-2 "account UUID"]
             [:td [:span.text-monospace user-id]]]
            [:tr
             [:td.col-sm-2 "email adress"]
             [:td [:span.text-monospace (:email me)]]]]]]]
        
        ; else - looking at a different user. TODO: is still needed, the page in /admin is more useful?
        (let [id (-> @routing/state* :route-params :user-id)]
          [:div
           [:h1 [:span "User "] [user-name-component]]
           [:p "user-id: " [:code id]]]))]
     [:div]]))
