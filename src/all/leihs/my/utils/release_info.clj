(ns leihs.my.utils.release-info
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.core :refer [keyword str presence]]

    [clojure.java.io :as io]
    [clojure.string :as str]
    [yaml.core :as yaml]
    ))


(defn update-version-build-fn [b]
  (if (= b "$TIMESTAMP$")
    (some-> "public/my/build-timestamp.txt"
            io/resource
            slurp
            str/trim)
    b))

(def leihs-my-version
  (-> "public/my/releases.yml"
      io/resource
      slurp
      yaml/parse-string
      :releases
      first
      (update :version_build update-version-build-fn)))


(def leihs-version
  (-> (some-> "public/my/leihs-releases.yml"
              io/resource
              slurp
              yaml/parse-string
              :releases
              first)
      (or {:version_major 5
           :version_minor 0
           :version_patch 0
           :version_pre "PRE"
           :version_build "$TIMESTAMP$"
           :description "" })
      (update :version_build update-version-build-fn)))

