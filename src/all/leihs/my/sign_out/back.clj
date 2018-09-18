(ns leihs.my.sign-out.back
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.core.sign-out.back :as sign-out]
    [leihs.core.sql :as sql]

    [leihs.my.paths :refer [path]]

    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect]]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]))


(def routes
  (cpj/routes
    (cpj/POST (path :sign-out) [] #'sign-out/ring-handler)))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
