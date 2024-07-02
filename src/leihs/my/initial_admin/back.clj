(ns leihs.my.initial-admin.back
  (:require
   [leihs.my.paths :refer [path]]
   [next.jdbc :as jdbc]
   [ring.util.response :refer [redirect]]))

(defn some-admin? [tx]
  (->> ["SELECT true AS has_admin FROM users WHERE is_admin = true"]
       (jdbc/execute-one! tx)
       (:has_admin)
       boolean))

; Redirect to /admin/initial-admin when no admin exists
(defn route
  ([handler request]
   (if (or (not= (-> request :accept :mime) :html)
           (= (:handler-key request) :initial-admin)
           (some-admin? (:tx request)))
     (handler request)
     (redirect (path :initial-admin) :see-other))))

(defn wrap [handler]
  (fn [request] (route handler request)))
