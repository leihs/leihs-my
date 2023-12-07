(ns leihs.my.resources.home.front
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   ["/my-ui" :as UI]
   [accountant.core :as accountant]
   [cljs.core.async :as async]

   [cljs.core.async :refer [timeout]]
   [cljs.pprint :refer [pprint]]
   [leihs.core.breadcrumbs :as breadcrumbs]
   [leihs.core.core :refer [keyword str presence]]
   [leihs.core.dom :as dom]
   [leihs.core.user.front :as user]
   [reagent.core :as reagent]))

(defn page []
  (let [page-props (dom/data-attribute "body" "page-props")]
    [:<>
     [:> UI/Components.HomePage page-props]
     [:script
      {:dangerouslySetInnerHTML
       {:__html "localStorage.clear(); sessionStorage.clear(); console.log('cleared browser storage')"}}]]))
