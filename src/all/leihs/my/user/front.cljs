(ns leihs.my.user.front
  (:refer-clojure :exclude [str keyword])
  (:require
    [leihs.core.breadcrumbs :as breadcrumbs]
    [leihs.core.core :refer [keyword str presence]]

    [leihs.my.paths :as paths :refer [path]]
    [leihs.my.user.api-tokens.breadcrumbs :as api-tokens-breadcrumbs]
    [leihs.my.user.password.breadcrumbs :as password-breadcrumbs]
    ))

(defn page []
  [:div.me
   (breadcrumbs/nav-component
     [(breadcrumbs/leihs-li)
      (breadcrumbs/me-user-li)]
     [(api-tokens-breadcrumbs/api-tokens-li)
      (password-breadcrumbs/password-li)
      ])
   [:h1 "me"]
   [:div
    [:p "There will be some awesome user information in the future here."]]])
