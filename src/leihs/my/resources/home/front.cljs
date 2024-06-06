(ns leihs.my.resources.home.front
  (:require
   ["/my-ui" :as UI]
   [leihs.core.dom :as dom]))

(defn page []
  (let [page-props (dom/data-attribute "body" "page-props")]
    [:<>
     [:> UI/Components.HomePage page-props]
     [:script
      {:dangerouslySetInnerHTML
       {:__html "localStorage.clear(); sessionStorage.clear(); console.log('cleared browser storage')"}}]]))
