(ns leihs.my.resources.home.back
  (:refer-clojure :exclude [str keyword])
  (:require [compojure.core :as cpj]
            [ring.util.response :refer [redirect]]
            [leihs.my.paths :refer [path]]
            [leihs.core.redirects :refer [redirect-target]]
            [leihs.core.core :refer [presence]]
            [leihs.core.release :as release]
            [leihs.core.anti-csrf.back :refer [anti-csrf-props]]
            [leihs.core.settings :refer [settings!]]
            [leihs.core.remote-navbar.shared :refer [navbar-props]]
            [leihs.my.back.html :refer [auth-page]]))

(defn home
  [request]
  (if-let [auth-entity (:authenticated-entity request)]
    (->> auth-entity
         (redirect-target (:tx request))
         redirect)
    (->> request
         navbar-props
         (hash-map :navbar)
         (merge {:footer {:appVersion release/version}})
         (merge (anti-csrf-props request))
         ((fn [props]
            (if-let [home-page-image-url (-> request :tx
                                             (settings! [:home_page_image_url])
                                             :home_page_image_url presence)]
              (assoc-in props [:splash :image] home-page-image-url)
              props)))
         auth-page)))


(def routes
  (cpj/routes
   (cpj/GET (path :home) [] #'home)
    ; (cpj/POST (path :sign-in) [] #'sign-in-post)
   ))

;#### debug ###################################################################
;(debug/debug-ns *ns*)
