(ns leihs.my.paths
  (:require
   [bidi.verbose :refer [branch leaf param]]
   [leihs.core.paths]))

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
          (leaf "/auth-info" :auth-info)
          (leaf "/language" :language)
          (branch "/debug"
                  (branch "/requests"
                          (leaf "/" :requests)
                          (branch "/" (param :id)
                                  (leaf "" :request))))))

(def password-restore-paths
  (branch "/"
          (leaf "forgot-password" :forgot-password)
          (leaf "reset-password" :reset-password)))

(def paths
  (branch ""
          leihs.core.paths/core-paths
          password-restore-paths
          (branch "/my"
                  my-service-paths)))

(reset! leihs.core.paths/paths* paths)

(def path leihs.core.paths/path)
