(ns leihs.my.navbar.front
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.routing.front :as routing]
    [leihs.core.user.front :as user]
    [leihs.my.paths :refer [path]]
    [environ.core :refer [env]]
    [cljs-http.client :as http-client]
    [reagent.core :as reagent]))

(def navbar* (reagent/atom nil))

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

(defn nav-component []
  (if (env :remote-navbar)
    [:div {:dangerouslySetInnerHTML {:__html @navbar*}}]
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
     [user/navbar-user-nav]]))

(defn init []
  (if (env :remote-navbar)
    (go (let [response (<! (http-client/get "/navbar"))]
          (if (= (:status response) 200)
            (reset! navbar* (:body response)))))))
