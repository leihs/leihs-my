(ns leihs.my.authorization)

(defn handler-is-ignored? [ignore-handler-keys request]
  (boolean (when-let [handler-key (:handler-key request)]
             (handler-key ignore-handler-keys))))

(defn authenticated-entity-not-present? [request]
  (not (contains? request :authenticated-entity)))

(defn wrap
  ([handler skip-authorization-handler-keys]
   (fn [request]
     (wrap request handler skip-authorization-handler-keys)))
  ([request handler skip-authorization-handler-keys]
   (cond
     (handler-is-ignored? skip-authorization-handler-keys request)
     (handler request)

     (authenticated-entity-not-present? request)
     {:status 401
      :body "Authentication required!"}

     :else
     (handler request))))

;#### debug ###################################################################
;(debug/debug-ns *ns*)
