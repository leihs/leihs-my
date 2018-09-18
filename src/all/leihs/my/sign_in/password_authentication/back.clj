(ns leihs.my.sign-in.password-authentication.back
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.core.password-authentication.back :as password-authentication]
    [leihs.core.session :as session]
    [leihs.core.sql :as sql]

    [leihs.my.back.html :as html]
    [leihs.my.paths :refer [path]]

    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect]]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]))


(def routes
  (cpj/routes
    (cpj/POST (path :password-authentication) [] #'password-authentication/ring-handler)))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
