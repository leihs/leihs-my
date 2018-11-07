(ns leihs.my.utils.redirects
  (:require [leihs.core.user.permissions :as user-perms]
            [clojure.tools.logging :as log]
            [leihs.my.paths :refer [path]]))

(defn redirect-target [tx user]
  (cond (:is_admin user) (path :admin)
        (user-perms/manager? tx user) (path :manage)
        :else (path :borrow)))
