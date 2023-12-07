(ns leihs.my.sign-in.front
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]])

  (:require
   ["/my-ui" :as UI]
   [leihs.core.constants]
   [leihs.core.dom :as dom]))

(defn page []
  (let [page-props (dom/data-attribute "body" "page-props")]
    (fn []
      [:> UI/Components.SignInPage page-props])))

