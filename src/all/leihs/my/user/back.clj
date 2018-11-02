(ns leihs.my.user.back
  (:require
    [leihs.core.sql :as sql]
    [leihs.my.paths :refer [path]]
    [leihs.my.user.shared :refer [wrap-me-id]]
    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [clojure.tools.logging :as log]
    [ring.util.response :refer [redirect]]))

(defn update-user
  [{tx :tx
    {user-id :user-id} :route-params
    {lang-id :language_id} :form-params
    {referer :referer} :headers}]
  (assert (= (jdbc/update! tx
                           :users
                           {:language_id lang-id}
                           ["id = ?" user-id])
             '(1)))
  (redirect referer))

(def routes
  (cpj/routes
    (cpj/POST (path :my-user) [] (-> update-user wrap-me-id))))
