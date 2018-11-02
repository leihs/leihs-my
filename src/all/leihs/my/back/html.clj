(ns leihs.my.back.html
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require [leihs.core.json :refer [to-json]]
            [leihs.core.http-cache-buster :as cache-buster :refer
             [wrap-resource]]
            [leihs.my.utils.release-info :as release-info]
            [leihs.core.sql :as sql]
            [leihs.core.url.core :as url]
            [leihs.my.server-side-js.engine :as js-engine]
            [leihs.core.anti-csrf.back :refer [anti-csrf-token]]
            [leihs.core.user.permissions :refer
             [borrow-access? managed-inventory-pools]]
            [leihs.core.user.permissions.procure :as procure]
            [leihs.my.paths :refer [path]]
            [clojure.java.jdbc :as jdbc]
            [hiccup.page :refer [include-js html5]]
            [environ.core :refer [env]]
            [clojure.tools.logging :as logging]
            [logbug.catcher :as catcher]
            [logbug.debug :as debug :refer [I>]]
            [logbug.ring :refer [wrap-handler-with-logging]]
            [logbug.thrown :as thrown]))

(defn include-site-css
  []
  (hiccup.page/include-css (cache-buster/cache-busted-path "/my/css/site.css")))

(defn include-font-css
  []
  (hiccup.page/include-css
    "/my/css/fontawesome-free-5.0.13/css/fontawesome-all.css"))

(defn head
  []
  [:head [:meta {:charset "utf-8"}]
   [:meta
    {:name "viewport",
     :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
   (include-site-css) (include-font-css)])

(defn body-attributes
  [request]
  {:data-user (some-> (:authenticated-entity request)
                      to-json
                      url/encode),
   :data-leihs-my-version (url/encode (to-json release-info/leihs-my-version)),
   :data-leihs-version (url/encode (to-json release-info/leihs-version))})

(defn not-found-handler
  [request]
  {:status 404,
   :headers {"Content-Type" "text/html"},
   :body (html5 (head)
                [:body (body-attributes request)
                 [:div.container-fluid
                  [:h1.text-danger "Error 404 - Not Found"]]])})

(defn- sub-apps
  [tx auth-entity]
  (if auth-entity
    (merge {:borrow (borrow-access? tx auth-entity)}
           {:admin (:is_admin auth-entity)}
           {:procure (procure/any-access? tx auth-entity)}
           {:manage (map #(hash-map :name (:name %)
                                    :href (path :daily
                                                {:inventory_pool_id (:id %)}))
                      (managed-inventory-pools tx auth-entity))})))

(defn render-navbar
  [request]
  (let [csrf-token (anti-csrf-token request)
        tx (:tx request)
        auth-entity (:authenticated-entity request)]
    (js-engine/render-react "Navbar"
                            {:config {:appTitle "Leihs",
                                      :appColor "gray",
                                      :csrfToken csrf-token,
                                      :subApps (sub-apps tx auth-entity)}})))

(defn html-handler
  [request]
  {:headers {"Content-Type" "text/html"},
   :body (html5 (head)
                [:body (body-attributes request)
                 [:div (render-navbar request)
                  [:div#app.container-fluid
                   [:div.alert.alert-warning [:h1 "Leihs My"]
                    [:p "This application requires Javascript."]]]]
                 (hiccup.page/include-js (cache-buster/cache-busted-path
                                           "/my/leihs-shared-bundle.js"))
                 (hiccup.page/include-js (cache-buster/cache-busted-path
                                           "/my/js/app.js"))])})


;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
