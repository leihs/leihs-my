(ns leihs.my.initial-admin.back
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
    [leihs.my.paths :refer [path]]
    [leihs.core.sql :as sql]

    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect]]

    [clojure.tools.logging :as logging]
    [logbug.debug :as debug])
  (:import
    [java.util UUID]
    ))

(defn password-hash
  ([password tx]
   (->> ["SELECT crypt(?,gen_salt('bf',10)) AS pw_hash" password]
        (jdbc/query tx)
        first :pw_hash)))

(defn some-admin? [tx]
  (->> ["SELECT true AS has_admin FROM users WHERE is_admin = true"]
       (jdbc/query tx ) first :has_admin boolean))

(defn prepare-data [data]
  (-> data
      (select-keys [:email])
      (assoc :is_admin true
             :id (UUID/randomUUID))))

(defn insert-user [data tx]
  (first (jdbc/insert! tx :users data)))

(defn insert-password [user data tx]
  (->> {:user_id (:id user)
        :authentication_system_id "password"
        :data (password-hash (:password data) tx)}
       (jdbc/insert! tx :authentication_systems_users)
       first))

(defn make-systemadmin [user tx]
  (->> {:user_id (:id user)}
       (jdbc/insert! tx :system_admins)
       first))

(defn create-initial-admin
  ([{tx :tx form-params :form-params data :body}]
   (create-initial-admin (if (empty? form-params)
                           data form-params) tx))
  ([data tx]
   (if (some-admin? tx)
     {:status 403
      :body "A admin user already exists!"}
     (let [user (-> data prepare-data (insert-user tx))]
       (assert user)
       (assert (insert-password user data tx))
       (assert (make-systemadmin user tx))
       (redirect (path :home) :see-other)))))

(def routes
  (cpj/routes
    (cpj/POST (path :initial-admin) [] create-initial-admin)))

(defn route
  ([handler request]
   (logging/debug 'request)
   (if (or (not= (-> request :accept :mime) :html)
           (= (:handler-key request) :initial-admin)
           (some-admin? (:tx request)))
     (handler request)
     (redirect (path :initial-admin) :see-other))))

(defn wrap [handler]
  (fn [request] (route handler request)))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)
