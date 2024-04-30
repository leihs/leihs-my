(ns leihs.my.initial-admin.back
  (:refer-clojure :exclude [keyword str])
  (:require
   [compojure.core :as cpj]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.my.paths :refer [path]]
   [leihs.my.user.shared :refer [set-password]]
   [next.jdbc :as jdbc]
   [ring.util.response :refer [redirect]]))

(defn some-admin? [tx]
  (->> ["SELECT true AS has_admin FROM users WHERE is_admin = true"]
       (jdbc/execute-one! tx)
       (:has_admin)
       boolean))

(defn prepare-data [data]
  (-> data
      (select-keys [:email])
      (assoc :is_admin true)
      (assoc :admin_protected true)
      (assoc :is_system_admin true)
      (assoc :system_admin_protected true)
      (assoc :lastname "Admin")
      (assoc :firstname "Initial")))

(defn insert-user [data tx]
  (let [query (-> (sql/insert-into :users)
                  (sql/values [data])
                  (sql/returning :*)
                  (sql-format))]
    (jdbc/execute-one! tx query)))

(defn make-procurement-admin [{user-id :id} tx]
  (-> (sql/insert-into :procurement_admins)
      (sql/values [{:user_id user-id}])
      (sql/returning :*)
      sql-format
      (->> (jdbc/execute-one! tx))))

(defn create-initial-admin
  ([{tx :tx form-params :form-params data :body}]
   (create-initial-admin (if (empty? form-params)
                           data form-params) tx))
  ([data tx]
   (if (some-admin? tx)
     {:status 403
      :body "An admin user already exists!"}
     (let [user (-> data prepare-data (insert-user tx))]
       (assert user)
       (assert (set-password (:id user)
                             (:password data)
                             tx))
       (assert (make-procurement-admin user tx))
       (redirect (path :home) :see-other)))))

(def routes
  (cpj/routes
   (cpj/POST (path :initial-admin) [] create-initial-admin)))

(defn route
  ([handler request]
   (if (or (not= (-> request :accept :mime) :html)
           (= (:handler-key request) :initial-admin)
           (some-admin? (:tx request)))
     (handler request)
     (redirect (path :initial-admin) :see-other))))

(defn wrap [handler]
  (fn [request] (route handler request)))

;#### debug ###################################################################
;(debug/debug-ns 'cider-ci.utils.shutdown)
;(debug/debug-ns *ns*)
