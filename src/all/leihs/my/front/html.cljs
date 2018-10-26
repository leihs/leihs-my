(ns leihs.my.front.html
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    )
  (:require
    [leihs.core.anti-csrf.front :as anti-csrf]
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.env :refer [use-remote-navbar?]]
    [leihs.core.remote-navbar.front :as remote-navbar]
    [leihs.core.requests.core :as requests]
    [leihs.core.requests.modal]
    [leihs.core.routing.front :as routing]
    [leihs.core.sign-in.front :as sign-in]
    [leihs.core.user.front :as user]

    [leihs.my.front.shared :refer [humanize-datetime-component short-id gravatar-url]]
    [leihs.my.front.state :as state]
    [leihs.my.paths :refer [path]]
    [leihs.my.sign-out.front :as sign-out]

    [clojure.pprint :refer [pprint]]
    [accountant.core :as accountant]
    [reagent.core :as reagent]
    ))

(defn li-navitem [handler-key display-string]
  (let [active? (= (-> @routing/state* :handler-key) handler-key)]
    [:li.nav-item
     {:class (if active? "active" "")}
     [:a.nav-link {:href (path handler-key)} display-string]]))

(defn li-admin-navitem []
  (let [active? (boolean
                  (when-let [current-path (-> @routing/state* :path)]
                    (re-matches #"^/admin.*$" current-path)))]
    [:li.nav-item
     {:class (if active? "active" "")}
     [:a.nav-link {:href (path :admin)} "Admin"]]))

(defn nav-bar []
  [:nav.navbar.navbar-expand.justify-content-between
   {:class "navbar-light bg-light"}
   [:a.navbar-brand {:href (path :home)} "leihs"]
   [:div
    (when @user/state*
      [:ul.navbar-nav
       [li-admin-navitem]
       [li-navitem :borrow "Borrow"]
       [li-navitem :lending "Lending"]
       [li-navitem :procure "Procurement"]
       ])]
   [user/navbar-user-nav]])

(defn version-component []
  [:span.navbar-text "Version "
   (let [major (:version_major @state/leihs-my-version*)
         minor (:version_minor @state/leihs-my-version*)
         patch (:version_patch @state/leihs-my-version*)
         pre (:version_pre @state/leihs-my-version*)
         build (:version_build @state/leihs-my-version*)]
     [:span
      [:span.major major]
      "." [:span.minor minor]
      "." [:span.patch patch]
      (when pre
        [:span "-"
         [:span.pre pre]])
      (when build
        [:span "+"
         [:span.build build]])])])

(defn footer-nav-component []
  [:nav.footer.navbar.navbar-expand-lg.navbar-dark.bg-secondary.col.mt-4
   [:div.col
    [:a.navbar-brand {:href (path :admin {})} "leihs-my"]
    [version-component]]
   [:div.col
    [:a.navbar-text
     {:href (path :auth-info)} "Auth-Info"]]
   [:div.col
    [:a.navbar-text
     {:href (path :status)} "Status-Info"]]
   [state/debug-toggle-navbar-component]
   [:form.form-inline {:style {:margin-left "0.5em"
                               :margin-right "0.5em"}}
    [:label.navbar-text
     [:a {:href (path :requests)}
      [requests/icon-component]
      " Requests "]]]])

(defn current-page []
  [:div
   [leihs.core.requests.modal/modal-component]
   (if (use-remote-navbar?)
     [remote-navbar/nav-component]
     [nav-bar])
   [:div
    (if-let [page (:page @routing/state*)]
      [page]
      [:div.page
       [:h1.text-danger "Application error: the current path can not be resolved!"]])]
   [state/debug-component]
   [footer-nav-component]])

(defn mount []
  (if (use-remote-navbar?) (remote-navbar/init))
  (when-let [app (.getElementById js/document "app")]
    (reagent/render [current-page] app))
  (accountant/dispatch-current!))
