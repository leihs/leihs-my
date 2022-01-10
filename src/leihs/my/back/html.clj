(ns leihs.my.back.html
  (:refer-clojure :exclude [str keyword])
  (:require
    [clojure.java.jdbc :as jdbc]
    [hiccup.page :refer [html5 include-js]]
    [leihs.core.json :refer [to-json]]
    [leihs.core.shared :refer [head]]
    [leihs.core.sql :as sql]
    [leihs.core.ssr :as ssr]
    [leihs.core.http-cache-buster2 :as cache-buster]
    [leihs.core.url.core :as url]
    [leihs.my.authorization :as auth]))

(defn route-user [request]
  (let [user-id (-> request :route-params :user-id)
        tx (:tx request)]
    (-> (sql/select :*)
        (sql/from :users)
        (sql/where [:= :id user-id])
        sql/format
        (->> (jdbc/query tx))
        first)))

(defn user-attribute [request]
  (let [user (if (auth/me? request)
               (:authenticated-entity request)
               (route-user request))]
    (-> user to-json url/encode)))

(defn body-attributes
  [request]
  {:data-user (user-attribute request)})

(defn not-found-handler
  [request]
  {:status 404,
   :headers {"Content-Type" "text/html"},
   :body (html5 (head)
                [:body (body-attributes request)
                 [:div.container-fluid
                  [:h1.text-danger "Error 404 - Not Found"]]])})



(defn spa-handler
  [request]
  {:headers {"Content-Type" "text/html"},
   :body (html5 (head
                  (hiccup.page/include-css
                    (cache-buster/cache-busted-path "/my/css/site.css")))
                [:body (body-attributes request)
                 [:div (ssr/render-navbar request)
                  [:div#app.container-fluid
                   [:div.alert.alert-warning [:h1 "Leihs My"]
                    [:p "This application requires Javascript."]]]]
                 (hiccup.page/include-js (cache-buster/cache-busted-path
                                           "/my/leihs-shared-bundle.js"))
                 (hiccup.page/include-js (cache-buster/cache-busted-path
                                           "/my/js/main.js"))])})


;#### debug ###################################################################
;(debug/debug-ns *ns*)
