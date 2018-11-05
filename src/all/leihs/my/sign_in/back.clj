(ns leihs.my.sign-in.back
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.sql :as sql]
    [leihs.my.paths :refer [path]]
    [leihs.my.sign-in.shared :refer [auth-system-base-query-for-unique-id]]

    [clojure.java.jdbc :as jdbc]
    [clojure.string :as str]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect]]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]))

(defn auth-system-query [unique-id]
  (-> unique-id
      auth-system-base-query-for-unique-id
      (sql/merge-select
        :authentication_systems.id
        :authentication_systems.type
        :authentication_systems.name
        :authentication_systems.description
        :authentication_systems.external_url)
      sql/format))

(defn auth-systems [tx unique-id]
  (->> unique-id auth-system-query (jdbc/query tx)))

(defn- render-sign-in-page [_] true) ; DUMMY

(defn sign-in-get [{tx :tx {user :user} :query-params}]
  (let [user-auth-systems (auth-systems tx user)]
    (if (= (count user-auth-systems) 1)
      (let [auth-system (first user-auth-systems)]
        (if (= (:type auth-system) "external")
          (redirect (:external_url auth-system))
          (render-sign-in-page user)))
      (render-sign-in-page user))))

(def routes
  (cpj/routes
    (cpj/GET (path :sign-in) [] #'sign-in-get)
    ; (cpj/POST (path :sign-in) [] #'sign-in-post)
    ))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
