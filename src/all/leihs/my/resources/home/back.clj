(ns leihs.my.resources.home.back
  (:refer-clojure :exclude [str keyword])
  (:require [compojure.core :as cpj]
            [leihs.core.ssr :as ssr]
            [leihs.my.paths :refer [path]]
            [leihs.core.redirects :refer [redirect-target]]
            [ring.util.response :refer [redirect]]))

(defn home
  [request]
  (if-let [auth-entity (:authenticated-entity request)]
    (->> auth-entity
         (redirect-target (:tx request))
         redirect)
    (ssr/render-root-page request)))

(def routes
  (cpj/routes
    (cpj/GET (path :home) [] #'home)
    ; (cpj/POST (path :sign-in) [] #'sign-in-post)
    ))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
