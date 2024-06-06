(ns leihs.my.routes
  (:refer-clojure :exclude [str keyword])
  (:require
   [leihs.core.routing.front :as routing]
   [leihs.my.front.state]
   [leihs.my.initial-admin.front :as initial-admin]
   [leihs.my.password-restore.front :as password-restore]
   [leihs.my.paths :as paths :refer [paths]]
   [leihs.my.resources.home.front :as home]
   [leihs.my.sign-in.front :as sign-in]
   [leihs.my.user.auth-info.front :as auth-info]
   [leihs.my.user.front :as user]
   [leihs.my.user.password.front :as password]))

(def resolve-table
  {:auth-info #'auth-info/page
   :home #'home/page
   :initial-admin #'initial-admin/page
   :my-user #'user/page
   :password #'password/page
   :sign-in #'sign-in/page
   :forgot-password #'password-restore/forgot-password
   :reset-password #'password-restore/reset-password})

(defn init []
  (routing/init paths resolve-table paths/external-handlers))
