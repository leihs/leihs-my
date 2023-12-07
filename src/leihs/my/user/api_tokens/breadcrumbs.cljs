(ns leihs.my.user.api-tokens.breadcrumbs
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [leihs.core.auth.core :as auth]
   [leihs.core.breadcrumbs :as breadcrumbs :refer [li]]
   [leihs.core.core :refer [keyword str presence]]
   [leihs.core.routing.front :as routing]
   [leihs.my.front.icons :as icons]))

(def api-token-route-params*
  (reaction
   {:user-id (or (-> @routing/state* :route-params :user-id) ":user-id")
    :api-token-id (or (-> @routing/state* :route-params :api-token-id) ":api-token-id")}))

(defn api-token-li []
  [li :api-token [:span [icons/api-token] " API-Token "]
   @api-token-route-params* {}
   :authorizers [auth/all-granted]])

(defn api-tokens-li []
  [li :api-tokens [:span [icons/api-token] " API-Tokens "]
   {:user-id (-> @routing/state* :route-params :user-id)} {}
   :authorizers [auth/all-granted]])

(defn api-token-add-li []
  [li :api-token-add [:span [icons/add] " Add API-Token "]
   @api-token-route-params* {}
   :authorizers [auth/all-granted]])

(defn api-token-edit-li []
  [li :api-token-edit [:span [icons/edit] " Edit API-Token "]
   @api-token-route-params* {}
   :authorizers [auth/all-granted]])

(defn api-token-delete-li []
  [li :api-token-delete [:span [icons/delete] " Delete API-Token "]
   @api-token-route-params* {}
   :authorizers [auth/all-granted]])

