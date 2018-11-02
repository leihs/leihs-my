(ns leihs.my.remote-navbar.back
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [hiccup.core :refer [html]]
            [leihs.my.paths :refer [path]]
            [leihs.my.server-side-js.engine :as js-engine]
            [leihs.core.sql :as sql]
            [leihs.core.user.permissions :refer
             [borrow-access? managed-inventory-pools]]
            [leihs.core.user.permissions.procure :as procure]))

; (defn- navbar-main-sections-html
;   [tx auth-entity]
;   (html
;     [:h3 {:style "color: red"} "Remote Navbar"]
;     [:ul
;      (if (borrow-access? tx auth-entity)
;        [:li [:a {:href (path :borrow)} "Borrow"]])
;      (if (:is_admin auth-entity) [:li [:a {:href (path :admin)} "Admin"]])
;      (if (procure/any-access? tx auth-entity)
;        [:li [:a {:href (path :procurement)} "Procurement"]])
;      (let [pools (managed-inventory-pools tx auth-entity)]
;        (if-not (empty? pools)
;          [:div "Pools:"
;           [:ul
;            (map #(vector :li
;                          [:a {:href (path :daily {:inventory_pool_id (:id %)})}
;                           (:name %)])
;              pools)]]))]))

(defn- sub-apps
  [tx auth-entity]
  [{:borrow (borrow-access? tx auth-entity)}
   {:admin (:is_admin auth-entity)}
   {:procure (procure/any-access? tx auth-entity)}
   {:manage (map #(hash-map :name (:name %)
                            :href (path :daily {:inventory_pool_id (:id %)}))
                 (managed-inventory-pools tx auth-entity))}])

; (defn- navbar-main-sections
;   [tx auth-entity]
;   [(if (borrow-access? tx auth-entity) {:title "Borrow", :href "/borrow"})])

(defn handler
  [request]
  (let [tx (:tx request)
        auth-entity (:authenticated-entity request)
        ; html-arg (navbar-main-sections tx auth-entity)
        ]
    {:headers {"Content-Type" "text/plain"},
     :body (js-engine/render-react "Navbar"
                                   {:config {:appTitle "Matus",
                                             :appColor "orange"
                                             :subApps (sub-apps tx auth-entity)}})}))
