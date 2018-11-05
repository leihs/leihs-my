(ns leihs.my.resources.home.back
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.sql :as sql]
    [leihs.my.paths :refer [path]]
    [leihs.my.sign-in.shared :refer [auth-system-base-query-for-unique-id]]
    [leihs.my.back.ssr :as ssr]

    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect]]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]))

(defn home
  [request]
  (ssr/render-root-page request))

(def routes
  (cpj/routes
    (cpj/GET (path :home) [] #'home)
    ; (cpj/POST (path :sign-in) [] #'sign-in-post)
    ))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
