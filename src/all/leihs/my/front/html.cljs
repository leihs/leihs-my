(ns leihs.my.front.html
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.anti-csrf.front :as anti-csrf]
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.requests.core :as requests]
    [leihs.core.requests.modal]
    [leihs.core.routing.front :as routing]
    [leihs.core.sign-in.front :as sign-in]

    [leihs.my.front.shared :refer [humanize-datetime-component short-id gravatar-url]]
    [leihs.my.front.state :as state]
    [leihs.my.navbar.front :as navbar]
    [leihs.my.paths :refer [path]]
    [leihs.my.sign-out.front :as sign-out]

    [cljs-http.client :as http-client]
    [clojure.pprint :refer [pprint]]
    [accountant.core :as accountant]
    [reagent.core :as reagent]
    ))

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
   [navbar/nav-component]
   [:div
    (if-let [page (:page @routing/state*)]
      [page]
      [:div.page
       [:h1.text-danger "Application error: the current path can not be resolved!"]])]
   [state/debug-component]
   [footer-nav-component]])

(defn mount []
  (navbar/init)
  (when-let [app (.getElementById js/document "app")]
    (reagent/render [current-page] app))
  (accountant/dispatch-current!))
