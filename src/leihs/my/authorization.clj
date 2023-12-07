(ns leihs.my.authorization
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require
   [clojure.tools.logging :as logging]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]))

(def HTTP-SAFE-VERBS #{:get :head :options :trace})

(defn http-safe? [request]
  (boolean (some-> request :request-method HTTP-SAFE-VERBS)))

(def HTTP-UNSAFE-VERBS #{:post :put :delete :patch})

(defn http-unsafe?  [request]
  (boolean (some-> request :request-method HTTP-UNSAFE-VERBS)))

(defn is-system-admin? [request]
  (boolean (-> request :authenticated-entity :is_system_admin)))

(defn is-not-system-admin? [request]
  (complement is-system-admin?))

(defn scope-read? [request]
  (boolean (some-> request :authenticated-entity :scope_read)))

(defn scope-write? [request]
  (boolean (some-> request :authenticated-entity :scope_write)))

(defn scope-system-admin-read? [request]
  (boolean (some-> request :authenticated-entity :scope_system_admin_read)))

(defn scope-system-admin-write? [request]
  (boolean (some-> request :authenticated-entity :scope_system_admin_write)))

(defn handler-is-ignored? [ignore-handler-keys request]
  (boolean (when-let [handler-key (:handler-key request)]
             (handler-key ignore-handler-keys))))

(defn authenticated-entity-not-present? [request]
  (not (contains? request :authenticated-entity)))

(defn http-safe-and-system-admin-with-read-scope?  [request]
  (boolean
   (and (http-safe? request)
        (is-system-admin? request)
        (scope-system-admin-read? request))))

(defn http-unsafe-and-system-admin-with-write-scope? [request]
  (boolean
   (and (http-unsafe? request)
        (is-system-admin? request)
        (scope-system-admin-write? request))))

(defn me? [request]
  (or (= (-> request :route-params :user-id) "me")
      (= (-> request :route-params :user-id)
         (-> request :authenticated-entity :id))))

(defn http-safe-and-me-user-with-read-scope? [request]
  (boolean
   (and (http-safe? request)
        (me? request)
        (scope-read? request))))

(defn http-unsafe-and-me-user-with-write-scope? [request]
  (boolean
   (and (http-unsafe? request)
        (me? request)
        (scope-write? request))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn wrap
  ([handler skip-authorization-handler-keys]
   (fn [request]
     (wrap request handler skip-authorization-handler-keys)))
  ([request handler skip-authorization-handler-keys]
   (cond
     (handler-is-ignored?
      skip-authorization-handler-keys request) (handler request)
     (authenticated-entity-not-present?
      request) {:status 401
                :body "Authentication required!"}
     ;;; me user ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
     (http-safe-and-me-user-with-read-scope?
      request) (handler request)
     (http-unsafe-and-me-user-with-write-scope?
      request) (handler request)
     ;;; admin ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
     (http-safe-and-system-admin-with-read-scope?
      request) (handler request)
     (http-unsafe-and-system-admin-with-write-scope?
      request) (handler request)
     ;;; other ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
     :else {:status 403
            :body "Forbidden!"})))

;#### debug ###################################################################
;(debug/debug-ns *ns*)
