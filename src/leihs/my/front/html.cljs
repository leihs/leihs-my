(ns leihs.my.front.html
  (:require
   ["/my-ui" :as UI]
   [accountant.core :as accountant]
   [leihs.core.dom :as dom]
   [leihs.core.requests.modal]
   [leihs.core.routing.front :as routing]
   [reagent.dom :as rdom]))

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
     [:div.m-5
      (if-let [page (:page @routing/state*)]
        [page]
        [:div.page
         [:h1.text-danger "Application error: the current path can not be resolved!"]])]]))

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
