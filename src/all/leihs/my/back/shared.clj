(ns leihs.my.back.shared
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])
  (:require [leihs.core.json :refer [to-json]]
            [leihs.core.http-cache-buster :as cache-buster :refer
             [wrap-resource]]
            [leihs.my.utils.release-info :as release-info]
            [leihs.core.sql :as sql]
            [leihs.core.ds :as ds]
            [leihs.core.url.core :as url]
            [leihs.my.server-side-js.engine :as js-engine]
            [leihs.core.anti-csrf.back :refer [anti-csrf-token]]
            [leihs.core.user.permissions :refer
             [borrow-access? managed-inventory-pools]]
            [leihs.core.user.permissions.procure :as procure]
            [leihs.my.paths :refer [path]]
            [clojure.java.jdbc :as jdbc]
            [hiccup.page :refer [include-js html5]]
            [environ.core :refer [env]]
            [clojure.tools.logging :as logging]
            [logbug.catcher :as catcher]
            [logbug.debug :as debug :refer [I>]]
            [logbug.ring :refer [wrap-handler-with-logging]]
            [logbug.thrown :as thrown]))

(defn include-site-css
  []
  (hiccup.page/include-css (cache-buster/cache-busted-path "/my/css/site.css")))

(defn include-font-css
  []
  (hiccup.page/include-css
    "/my/css/fontawesome-free-5.0.13/css/fontawesome-all.css"))

(defn head
  []
  [:head [:meta {:charset "utf-8"}]
   [:meta
    {:name "viewport",
     :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
   (include-site-css) (include-font-css)])


;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
