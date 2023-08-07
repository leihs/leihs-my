(ns leihs.my.sign-in.front
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]])

  (:require
   [leihs.core.constants]
   [leihs.core.dom :as dom]
   ["/my-ui" :as UI]))


(defn page []
  (let [page-props (dom/data-attribute "body" "page-props")]
    (fn []
      [:> UI/Components.SignInPage page-props])))

