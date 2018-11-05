(ns leihs.my.back.ssr
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require [leihs.core.json :refer [to-json]]
            [leihs.core.http-cache-buster :as cache-buster :refer
             [wrap-resource]]
            [leihs.my.utils.release-info :as release-info]
            [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [leihs.core.url.core :as url]
            [leihs.my.back.shared :refer [head]]
            [leihs.my.server-side-js.engine :as js-engine]
            [leihs.core.anti-csrf.back :refer [anti-csrf-token]]
            [leihs.core.user.permissions :refer
             [borrow-access? managed-inventory-pools]]
            [leihs.core.user.permissions.procure :as procure]
            [leihs.my.paths :refer [path]]

            [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]
            [hiccup.page :refer [include-js html5]]
            [clojure.tools.logging :as log]
            [logbug.catcher :as catcher]
            [logbug.debug :as debug :refer [I>]]
            [logbug.thrown :as thrown]))

(defn- render-page-base
  [inner-html]
  (html5 (head)
         [:body
          [:noscript "This application requires Javascript."] inner-html
          (hiccup.page/include-js (cache-buster/cache-busted-path
                                    "/my/leihs-shared-bundle.js"))]))

(defn- languages
  [tx]
  (-> (sql/select :*)
      (sql/from :languages)
      (sql/where [:= :active true])
      sql/format
      (->> (jdbc/query tx))))

(defn- auth-systems
  [tx]
  (-> (sql/select :id :name
                  :description :type
                  :priority :shortcut_sign_in_enabled)
      (sql/from :authentication_systems)
      (sql/where [:= :enabled true])
      sql/format
      (->> (jdbc/query tx))))

(comment (auth-systems (ds/get-ds)) (languages (ds/get-ds)))

(defn- sub-apps
  [tx auth-entity]
  (if auth-entity
    (merge {:borrow (borrow-access? tx auth-entity)}
           {:admin (:is_admin auth-entity)}
           {:procure (procure/any-access? tx auth-entity)}
           {:manage (map #(hash-map :name (:name %)
                                    :href (path :daily
                                                {:inventory_pool_id (:id %)}))
                      (managed-inventory-pools tx auth-entity))})))

(defn- user-info
  [auth-entity]
  (if auth-entity
    {:user {:id (:user_id auth-entity),
            :firstname (:firstname auth-entity),
            :lastname (:lastname auth-entity),
            :login (:login auth-entity),
            :email (:email auth-entity)
            :selectedLocale (:language_id auth-entity)}}))

(defn- navbar-props
  [request]
  (let [csrf-token (anti-csrf-token request)
        tx (:tx request)
        auth-entity (:authenticated-entity request)]
    {:config {:appTitle "Leihs",
              :appColor "gray",
              :csrfToken csrf-token,
              :me (user-info auth-entity),
              :subApps (sub-apps tx auth-entity),
              :locales (languages tx)}}))

(defn render-navbar
  [request]
  (js-engine/render-react "Navbar" (navbar-props request)))

(defn render-root-page
  [request]
  (render-page-base (js-engine/render-react "HomePage"
                                            {:navbar (navbar-props request)})))

(defn render-sign-in-page
  [user-param request flash]
  (let [tx (:tx request)
        auth-entity (:authenticated-entity request)]
    (render-page-base (js-engine/render-react "SignInPage"
                                              {:navbar (navbar-props request),
                                               :authSystems (auth-systems tx),
                                               :authFlow {:user user-param}
                                               :flash flash}))))


;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
