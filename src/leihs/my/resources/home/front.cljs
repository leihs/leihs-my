(ns leihs.my.resources.home.front
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]]
   [cljs.core.async.macros :refer [go]])
  (:require
   [leihs.core.breadcrumbs :as breadcrumbs]
   [leihs.core.core :refer [keyword str presence]]
   [leihs.core.user.front :as user]

   [accountant.core :as accountant]
   [cljs.core.async :as async]
   [cljs.core.async :refer [timeout]]
   [cljs.pprint :refer [pprint]]
   [reagent.core :as reagent]
   [leihs.core.dom :as dom]
   ["/my-ui" :as UI]))

(defn page []
  (let [page-props (dom/data-attribute "body" "page-props")]
    [:<>
     [:> UI/Components.HomePage page-props]
     [:script
      {:dangerouslySetInnerHTML
       {:__html "localStorage.clear(); sessionStorage.clear(); console.log('cleared browser storage')"}}]]))
