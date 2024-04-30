(ns leihs.my.back.html
  (:refer-clojure :exclude [keyword str])
  (:require
   [hiccup.page :refer [html5]]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.core.http-cache-buster2 :as cache-buster]
   [leihs.core.json :refer [to-json]]
   [leihs.core.remote-navbar.shared :refer [navbar-props]]
   [leihs.core.shared :refer [head]]
   [leihs.core.url.core :as url]
   [leihs.my.authorization :as auth]
   [next.jdbc :as jdbc]))

(defn route-user [request]
  (let [user-id (-> request :route-params :user-id)
        tx (:tx request)]
    (-> (sql/select :*)
        (sql/from :users)
        (sql/where [:= :id [:cast user-id :uuid]])
        sql-format
        (->> (jdbc/execute-one! tx)))))

(defn user-attribute [request]
  (let [user (if (auth/me? request)
               (:authenticated-entity request)
               (route-user request))]
    (-> user to-json url/encode)))

(defn navbar-attribute [request]
  (let [navbar (navbar-props request {})]
    (-> navbar to-json url/encode)))

(defn body-attributes
  [request]
  {:data-user (user-attribute request)
   :data-navbar (navbar-attribute request)})

(defn not-found-handler
  [request]
  {:status 404,
   :headers {"Content-Type" "text/html"},
   :body (html5 (head)
                [:body (body-attributes request)
                 [:div.container-fluid
                  [:h1.text-danger "Error 404 - Not Found"]]])})

; renders layout for auth pages (sign-in, password-restore) and home
(defn auth-page [props]
  (html5 (head
          (hiccup.page/include-css (cache-buster/cache-busted-path "/my/ui/my-ui.css")))

         [:body {:class "bg-paper"
                 :data-page-props (-> props to-json url/encode)}

          [:noscript "This application requires Javascript."]

          [:div#app]

          (hiccup.page/include-js (cache-buster/cache-busted-path "/my/js/main.js"))]))

; renders admin layout for pages like /my/user/me
(defn spa-handler
  [request]
  {:headers {"Content-Type" "text/html"},
   :body (html5 (head
                 (hiccup.page/include-css
                  (cache-buster/cache-busted-path "/my/ui/my-ui.css")))
                [:body (body-attributes request)

                 [:div
                  [:div#app.container-fluid
                   [:div.alert.alert-warning [:h1 "Leihs My"]
                    [:p "This application requires Javascript."]]]]

                 (hiccup.page/include-js (cache-buster/cache-busted-path
                                          "/my/js/main.js"))])})

;#### debug ###################################################################
;(debug/debug-ns *ns*)
