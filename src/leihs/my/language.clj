(ns leihs.my.language
  (:require
   [compojure.core :as cpj]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.core.locale :refer [set-language-cookie]]
   [leihs.my.paths :refer [path]]
   [next.jdbc :as jdbc]
   [ring.util.response :refer [redirect]]))

(defn redirect-back-with-language-cookie
  [{tx :tx
    {locale :locale} :form-params
    {referer "referer"} :headers}]
  (let [result (-> (sql/select :*)
                   (sql/from :languages)
                   (sql/where [:= :locale locale])
                   sql-format
                   (->> (jdbc/execute-one! tx)))]
    (-> (redirect referer)
        (set-language-cookie result))))

(def routes
  (cpj/routes
   (cpj/POST (path :language) [] #'redirect-back-with-language-cookie)))
