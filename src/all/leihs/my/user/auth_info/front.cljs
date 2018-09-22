(ns leihs.my.user.auth-info.front
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.requests.core :as requests]
    [leihs.core.routing.front :as routing]
    [leihs.core.breadcrumbs :as breadcrumbs]

    [leihs.my.front.shared :refer [humanize-datetime-component]]
    [leihs.my.front.state :as state]
    [leihs.my.paths :as paths :refer [path]]

    [accountant.core :as accountant]
    [cljs.core.async :as async]
    [cljs.pprint :refer [pprint]]
    [reagent.core :as reagent]

    ))


(def auth-info-data* (reagent/atom nil))

(def fetch-auth-info-id* (reagent/atom nil))

(defn fetch-auth-info [& args]
  (let [resp-chan (async/chan)
        id (requests/send-off {:url (path :auth-info {:user-id "me"})
                               :method :get}
                              {:modal false
                               :title "Fetch Auth-Info"
                               :handler-key :api-token
                               :retry-fn #'fetch-auth-info}
                              :chan resp-chan)]
    (reset! fetch-auth-info-id* id)
    (go (let [resp (<! resp-chan)]
          (when (and (= (:status resp) 200) ;success
                     (= id @fetch-auth-info-id*)) ;still the most recent request
            (reset! auth-info-data* (->> resp :body)))))))

(defn page []
  [:div.auth-info
   [routing/hidden-state-component
    {:will-mount (fn [] (reset! auth-info-data* nil))
     :did-change fetch-auth-info 
     :did-mount fetch-auth-info}]
   [:h1 "Auth-Info"]
   [:p "The data shown below is mostly of interest for exploring the API or for debugging."]
   [:pre.auth-info-data (with-out-str (pprint @auth-info-data*))]
   ])
