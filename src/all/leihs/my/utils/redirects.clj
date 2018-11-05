(ns leihs.my.utils.redirects
  (:require [leihs.core.user.permissions :as user-perms]
            [leihs.my.paths :refer [path]]))

(defn redirect-target [user]
  (cond (:is_admin user) (path :admin)
        (user-perms/manager? user) (path :manage)
        :else (path :borrow)))
