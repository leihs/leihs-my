(ns leihs.my.scratch
  (:refer-clojure :exclude [str keyword])
  (:require [leihs.core.core :refer [keyword str presence]])

  (:require
    [buddy.sign.jwt :as jwt]
    [buddy.core.keys :as keys]
    [clj-time.core :as time]

    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug :refer [I>]]
    [logbug.thrown :as thrown]
    )
  )

(def ec-priv-key
  (->
    (->>
      (-> "
          -----BEGIN EC PRIVATE KEY-----
          MHcCAQEEIHErTjw8Z1yNisngEuZ5UvBn1qM2goN3Wd1V4Pn3xQeYoAoGCCqGSM49
          AwEHoUQDQgAEzGT0FBI/bvn21TOuLmkzDwzRsIuOyIf9APV7DAZr3fgCqG1wzXce
          MGG42wJIDRduJ9gb3LJiewqzq6VVURvyKQ==
          -----END EC PRIVATE KEY-----
          "
          (clojure.string/split #"\n"))
      (map clojure.string/trim)
      (map presence)
      (filter identity)
      (clojure.string/join "\n"))
    (keys/str->private-key)))

(def ec-pub-key
  (->
    (->>
      (-> "
          -----BEGIN PUBLIC KEY-----
          MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEzGT0FBI/bvn21TOuLmkzDwzRsIuO
          yIf9APV7DAZr3fgCqG1wzXceMGG42wJIDRduJ9gb3LJiewqzq6VVURvyKQ==
          -----END PUBLIC KEY-----
          "
          (clojure.string/split #"\n"))
      (map clojure.string/trim)
      (map presence)
      (filter identity)
      (clojure.string/join "\n"))
    keys/str->public-key))


(def claims
  {:email "user@example.com"
   :org_id "12345"
   :exp (time/plus (time/now) (time/seconds 1))
   :iat (time/now)
   })

(def token (jwt/sign claims ec-priv-key {:alg :es256}))
;(def unsigned-token (jwt/unsign token ec-pub-key {:alg :es256}))


;#### debug ###################################################################
;(debug/debug-ns *ns*)
