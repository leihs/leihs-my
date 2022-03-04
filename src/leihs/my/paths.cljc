(ns leihs.my.paths
  (:refer-clojure :exclude [str keyword])
  (:require
    [bidi.bidi :refer [path-for match-route]]
    [bidi.verbose :refer [branch param leaf]]
    [clojure.pprint :refer [pprint]]
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.paths]
    [taoensso.timbre :as logging]
    ))

(def external-handlers
  #{:admin
    :borrow
    :daily
    :forgot-password
    :home
    :manage
    :procurement
    :reset-password
    :sign-in
    :status})

;(re-matches
;  #"^\/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$|^$"
;  "/6ba7b810-9dad-11d1-80b4-00c04fd430c8")
;(re-matches
;  #"^\/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$|^$"
;  "abc")

(def my-service-paths
  (branch ""
          (leaf "/status" :status)
          (leaf "/initial-admin" :initial-admin)
          (branch "/debug"
                  (branch "/requests"
                          (leaf "/" :requests)
                          (branch "/" (param :id)
                                  (leaf "" :request))))))
(def api-tokens-paths
  (branch "/api-tokens/"
          (leaf "" :api-tokens)
          (leaf "add" :api-token-add)
          (branch ""
                  (param :api-token-id)
                  (leaf "" :api-token)
                  (leaf "/delete" :api-token-delete)
                  (leaf "/edit" :api-token-edit))))

(def user-paths
  (branch "/user/"
          (param [#"([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})|(me)|(:user-id)" :user-id])
          (leaf "/password" :password)
          api-tokens-paths))

(def password-restore-paths
  (branch "/"
          (leaf "forgot-password" :forgot-password)
          (leaf "reset-password" :reset-password)))

(def paths
  (branch ""
          leihs.core.paths/core-paths
          password-restore-paths
          (branch "/my"
                  (leaf "/language" :language)
                  my-service-paths
                  user-paths)))

(reset! leihs.core.paths/paths* paths)

(def path leihs.core.paths/path)

;(path :user {:user-id "me"}{})
;(path :my-user {:user-id "me"}{})
