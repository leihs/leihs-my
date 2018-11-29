(ns leihs.my.utils.redirects
  (:require [leihs.core.user.permissions :as user-perms]
            [leihs.core.user.permissions.procure :as procure-perms]
            [clojure.tools.logging :as log]
            [leihs.my.paths :refer [path]]))

(defn redirect-target [tx user]
  (cond (user-perms/sysadmin? tx user) (path :admin)
        (:is_admin user) (path :admin)
        (user-perms/manager? tx user) (path :manage)
        (user-perms/borrow-access? tx user) (path :borrow)
        (procure-perms/any-access? tx user) (path :procurement)
        :else (path :my-user)))
