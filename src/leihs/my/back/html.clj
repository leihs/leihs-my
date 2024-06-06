(ns leihs.my.back.html
  (:require
   [hiccup.page :refer [html5]]
   [leihs.core.http-cache-buster2 :as cache-buster]
   [leihs.core.json :refer [to-json]]
   [leihs.core.remote-navbar.shared :refer [navbar-props]]
   [leihs.core.shared :refer [head]]
   [leihs.core.url.core :as url]))

(defn user-attribute [request]
  (let [user (:authenticated-entity request)]
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

; renders admin layout for pages like /my/auth-info
(defn spa-handler
  [request]
  {:headers {"Content-Type" "text/html"},
   :body
   (html5 (head
           (hiccup.page/include-css (cache-buster/cache-busted-path "/my/ui/my-ui.css")))

          [:body.bg-paper (body-attributes request)

           [:noscript "This application requires Javascript."]

           [:div#app]

           (hiccup.page/include-js (cache-buster/cache-busted-path "/my/js/main.js"))])})

;#### debug ###################################################################
;(debug/debug-ns *ns*)
