(ns leihs.my.front.breadcrumbs
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.icons :as icons]
    [leihs.core.breadcrumbs :refer [li]]
    ))

(defn auth-info-li [] (li :auth-info "Info"))
(defn auth-li [] (li :auth "Authentication"))
(defn auth-password-sign-in-li [] (li :auth-password-sign-in "Password sign-in"))



