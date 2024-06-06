(ns leihs.my.auth-info.front

  (:require
   [cljs.core.async :as async :refer [go <!]]
   [cljs.pprint :refer [pprint]]
   [leihs.core.requests.core :as requests]
   [leihs.core.routing.front :as routing]
   [leihs.my.paths :as paths :refer [path]]
   [reagent.core :as reagent]))

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
    {:did-mount (fn []
                  (reset! auth-info-data* nil)
                  (fetch-auth-info))
     :did-change fetch-auth-info}]
   [:h1 "Auth-Info"]
   [:p "The data shown below is mostly of interest for exploring the API or for debugging."]
   [:pre.auth-info-data (with-out-str (pprint @auth-info-data*))]])
