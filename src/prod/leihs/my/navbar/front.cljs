(ns leihs.my.navbar.front
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs-http.client :as http-client]
    [reagent.core :as reagent]))

(def navbar* (reagent/atom nil))

(defn nav-component []
  [:div {:dangerouslySetInnerHTML {:__html @navbar*}}])

(defn init []
  (go (let [response (<! (http-client/get "/navbar"))]
        (if (= (:status response) 200)
          (reset! navbar* (:body response))))))
