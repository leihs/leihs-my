(ns leihs.my.user.back
  (:require
    [leihs.core.sql :as sql]
    [leihs.my.paths :refer [path]]
    [leihs.my.user.shared :refer [wrap-me-id]]
    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [ring.util.response :refer [redirect set-cookie]]
    [taoensso.timbre :refer [debug info warn error spy]]
    ))

(defn update-user
  [{tx :tx
    {user-id :user-id} :route-params
    {locale :locale} :form-params
    {referer "referer"} :headers
    :as request}]
  (when user-id
    (assert (= (jdbc/update! tx
                             :users
                             {:language_locale locale}
                             ["id = ?" user-id])
               '(1))))
  (redirect referer))

(def routes
  (cpj/routes
    (cpj/POST (path :my-user) [] (-> update-user wrap-me-id))))
