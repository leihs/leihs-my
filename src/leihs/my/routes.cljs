(ns leihs.my.routes
  (:require
   [leihs.core.routing.front :as routing]
   [leihs.my.auth-info.front :as auth-info]
   [leihs.my.initial-admin.front :as initial-admin]
   [leihs.my.password-restore.front :as password-restore]
   [leihs.my.paths :as paths :refer [paths]]
   [leihs.my.resources.home.front :as home]
   [leihs.my.sign-in.front :as sign-in]))

(def resolve-table
  {:auth-info #'auth-info/page
   :home #'home/page
   :initial-admin #'initial-admin/page
   :sign-in #'sign-in/page
   :forgot-password #'password-restore/forgot-password
   :reset-password #'password-restore/reset-password})

(defn init []
  (routing/init paths resolve-table paths/external-handlers))
