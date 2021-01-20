(ns leihs.my.front.state
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.dom :as dom]
    [leihs.core.routing.front :as routing]
    [leihs.core.user.front :as user]

    [cljsjs.moment]
    [clojure.pprint :refer [pprint]]
    [reagent.core :as reagent]
    ))


(defonce global-state* (reagent/atom {:debug false
                                      :users-query-params {}
                                      :timestamp (js/moment)}))

(js/setInterval #(swap! global-state*
                       (fn [s] (merge s {:timestamp (js/moment)}))) 1000)

(def settings* (reagent/atom (dom/data-attribute "body" "settings")))

(def leihs-my-version* (reagent/atom (dom/data-attribute "body" "leihs-my-version")))

(def leihs-version* (reagent/atom (dom/data-attribute "body" "leihs-version")))

(def debug?* (reaction (:debug @global-state*)))

(defn update-state [state-ref key-seq fun]
  (swap! state-ref
         (fn [cs]
           (assoc-in cs key-seq
                     (fun (get-in cs key-seq nil))))))

(defn debug-toggle-navbar-component []
  [:form.form-inline
   [:input#toggle-debug
    {:type :checkbox
     :checked (-> @global-state* :debug boolean)
     :on-click #(update-state global-state*
                               [:debug]
                               (fn [v] (not v)))}]
   [:label.navbar-text {:for "toggle-debug"
                        :style {:padding-left "0.25em"}} " debug "]])

(defn debug-component []
  (when (:debug @global-state*)
    [:div.debug
     [:hr]
     [:h2 "Debug State"]
     [:div
      [:h3 "@global-state*"]
      [:pre (with-out-str (pprint @global-state*))]]
     [:div
      [:h3 "@leihs-my-version"]
      [:pre (with-out-str (pprint @leihs-my-version*))]]
     [:div
      [:h3 "@leihs-version"]
      [:pre (with-out-str (pprint @leihs-version*))]]
     [:div
      [:h3 "@routing/state*"]
      [:pre (with-out-str (pprint @routing/state*))]]
     [:div
      [:h3 "@user*"]
      [:pre (with-out-str (pprint @user/state*))]]
     [:div
      [:h3 "@settings*"]
      [:pre (with-out-str (pprint @settings*))]]]))
