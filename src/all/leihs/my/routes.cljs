(ns leihs.my.routes
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    )
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.routing.front :as routing]

    [leihs.my.front.state]
    [leihs.my.initial-admin.front :as initial-admin]
    [leihs.my.paths :as paths :refer [path paths]]
    [leihs.my.resources.home.front :as home]
    [leihs.my.resources.status.front :as status]
    [leihs.my.sign-in.external-authentication.front :as external-authentication]
    [leihs.my.sign-in.front :as sign-in]
    [leihs.my.user.api-token.front :as api-token]
    [leihs.my.user.api-tokens.front :as api-tokens]
    [leihs.my.user.auth-info.front :as auth-info]
    [leihs.my.user.front :as user]
    [leihs.my.user.password.front :as password]

    [cljsjs.js-yaml]
    [clojure.pprint :refer [pprint]]
    [reagent.core :as reagent]
    ))

(def resolve-table
  {
   :api-token #'api-token/show-page
   :api-token-add #'api-token/add-page
   :api-token-delete #'api-token/delete-page
   :api-token-edit #'api-token/edit-page
   :api-tokens #'api-tokens/page
   :auth-info #'auth-info/page
   :external-authentication-sign-in #'external-authentication/sign-in-page
   :home #'home/page
   :initial-admin #'initial-admin/page
   :my-user #'user/page
   :password #'password/page
   :sign-in #'sign-in/page
   :status #'status/info-page
   })


(defn init []
  (routing/init paths resolve-table paths/external-handlers))
