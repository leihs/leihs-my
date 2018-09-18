(ns leihs.my.paths
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.paths]

    [bidi.bidi :refer [path-for match-route]]
    [bidi.verbose :refer [branch param leaf]]

    #?@(:clj
         [[uritemplate-clj.core :as uri-templ]
          [clojure.tools.logging :as logging]
          [logbug.catcher :as catcher]
          [logbug.debug :as debug]
          [logbug.thrown :as thrown]

          ])))

(def external-handlers
  #{:borrow
    :lending
    :procurement
    })

;(re-matches
;  #"^\/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$|^$"
;  "/6ba7b810-9dad-11d1-80b4-00c04fd430c8")
;(re-matches
;  #"^\/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$|^$"
;  "abc")

(def my-service-paths
  (branch "/my"
          (leaf "/status" :status)
          (leaf "/shutdown" :shutdown)
          (leaf "/initial-admin" :initial-admin)
          (branch "/debug"
                  (branch "/requests"
                          (leaf "/" :requests)
                          (branch "/" (param :id)
                                  (leaf "" :request))))))


(def paths
  (branch ""
          leihs.core.paths/core-paths
          my-service-paths
          (branch "/user/"
                  (param [#"([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})|(me)" :user-id])
                  (leaf "" :user)
                  (leaf "/auth-info" :auth-info)
                  )))

(reset! leihs.core.paths/paths* paths) 

;(match-route paths "/procure")

;(match-route paths "/user/6ba7b810-9dad-11d1-80b4-00c04fd430c8")
;(match-route paths "/user/me")


(def path leihs.core.paths/path)

;(path :user {} {})

;(path :auth-info {:user-id "6ba7b810-9dad-11d1-80b4-00c04fd430c8"})
;(path :initial-admin)
;(path :sign-in)

