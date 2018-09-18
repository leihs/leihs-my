(ns leihs.my.front.breadcrumbs
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.routing.front :as routing]

    [leihs.my.front.icons :as icons]
    [leihs.my.front.state :as state]
    [leihs.my.paths :as paths :refer [path]]))

(defn li
  ([k n]
   (li k n {} {}))
  ([handler-key inner route-params query-params]
   (let [active? (= (-> @routing/state* :handler-key) handler-key)]
     [:li.breadcrumb-item {:key handler-key :class (if active? "active" "")}
      (if active?
        [:span inner]
        [:a {:href (path handler-key route-params query-params)} inner])])))

(defn admin-li [] (li :admin [:span icons/admin " Admin "]))
(defn auth-info-li [] (li :auth-info "Info"))
(defn auth-li [] (li :auth "Authentication"))
(defn auth-password-sign-in-li [] (li :auth-password-sign-in "Password sign-in"))
(defn borrow-li [] (li :borrow "Borrow"))
(defn debug-li [] (li :debug "Debug"))
(defn email-li [address] [:li.breadcrumb-item {:key (str "mailto:" address )} [:a {:href (str "mailto:" address )} [:i.fas.fa-envelope] " Email "]])
(defn initial-admin-li [] (li :initial-admin "Initial-Admin"))
(defn leihs-li [] (li :home [:span icons/home " Home "]))
(defn lending-li [] (li :lending "Lending"))
(defn procurement-li [] (li :procurement "Procurement"))
(defn request-li [id] (li :request "Request" {:id id} {}))
(defn requests-li [] (li :requests "Requests"))

(defn nav-component [left right]
  [:div.row.nav-component.mt-3
   [:nav.col-lg {:aria-label :breadcrumb :role :navigation}
    (when (seq left)
      [:ol.breadcrumb
       (for [li left] li) ])]
   [:nav.col-lg {:role :navigation}
    (when (seq right)
      [:ol.breadcrumb.leihs-nav-right
       (for [li right] li)])]])
