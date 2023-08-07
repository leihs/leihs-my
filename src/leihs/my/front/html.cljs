(ns leihs.my.front.html
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [accountant.core :as accountant]
   [leihs.core.requests.core :as requests]
   [leihs.core.requests.modal]
   [leihs.core.routing.front :as routing]
   [leihs.my.front.state :as state]
   [leihs.my.paths :refer [path]]
   [reagent.dom :as rdom]
   [leihs.core.dom :as dom]
   ["/my-ui" :as UI]))

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

(def auth-page-handler-keys
  #{:home
    :sign-in
    :forgot-password
    :reset-password})

(defn admin-page []
  (let [navbar-data (dom/data-attribute "body" "navbar")]
    [:div
     [leihs.core.requests.modal/modal-component]
     [:> UI/Components.Navbar navbar-data]
     [:div
      (if-let [page (:page @routing/state*)]
        [page]
        [:div.page
         [:h1.text-danger "Application error: the current path can not be resolved!"]])]
     [:<>
      [state/debug-component]
      [footer-nav-component]]]))

(defn auth-page []
  (if-let [page (:page @routing/state*)]
    [page]
    [:div.page
     [:h1.text-danger "Application error: the current path can not be resolved!"]]))

(defn current-page []
  (let [handler-key (or (:handler-key @routing/state*) :not-found)
        auth-page? (handler-key auth-page-handler-keys)]
    (if auth-page?
      (auth-page)
      (admin-page))))

(defn mount []
  (when-let [app (.getElementById js/document "app")]
    (rdom/render [current-page] app))
  (accountant/dispatch-current!))
