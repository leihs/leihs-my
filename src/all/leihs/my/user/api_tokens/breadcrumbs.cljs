(ns leihs.my.user.api-tokens.breadcrumbs
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.icons :as icons]
    [leihs.core.routing.front :as routing]
    [leihs.core.breadcrumbs :as breadcrumbs :refer [li]]))


(def api-token-route-params*
  (reaction
    {:user-id (-> @routing/state* :route-params :user-id) 
     :api-token-id (-> @routing/state* :route-params :api-token-id)}))

(defn api-token-li []
  (li :api-token [:span icons/api-token " API-Token "] 
      @api-token-route-params* {}))

(defn api-tokens-li []
  (li :api-tokens [:span icons/api-token " API-Tokens "] 
      {:user-id (-> @routing/state* :route-params :user-id)} {}))

(defn api-token-add-li []
  (li :api-token-add [:span icons/add " Add API-Token "] 
      @api-token-route-params* {}))

(defn api-token-edit-li []
  (li :api-token-edit [:span icons/edit " Edit API-Token "]
      @api-token-route-params* {}))

(defn api-token-delete-li []
  (li :api-token-delete [:span icons/delete " Delete API-Token "]
      @api-token-route-params* {}))

