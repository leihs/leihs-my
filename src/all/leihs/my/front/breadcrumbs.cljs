(ns leihs.my.front.breadcrumbs
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.auth.core :as auth]
    [leihs.core.breadcrumbs :refer [li]]
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.icons :as icons]
    [leihs.core.routing.front :as routing]
    ))

(defn auth-info-li [] (li :auth-info "Info"))
(defn auth-li [] (li :auth "Authentication"))
(defn auth-password-sign-in-li [] (li :auth-password-sign-in "Password sign-in"))

(defn user-li []
  [li :my-user [:span icons/user " User " ]
   {:user-id (-> @routing/state* :route-params :user-id)}{}
   :authorizers [auth/all-granted]])
