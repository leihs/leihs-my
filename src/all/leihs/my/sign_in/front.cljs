(ns leihs.my.sign-in.front
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.requests.core :as requests]
    [leihs.core.routing.front :as routing]
    [leihs.core.sign-in.external-authentication.front :as external-authentication]
    [leihs.core.sign-in.front :as sign-in]
    [leihs.core.sign-in.password-authentication.front :as password-authentication]

    [leihs.my.front.breadcrumbs :as breadcrumbs]
    [leihs.my.front.state :as state]
    [leihs.my.paths :as paths :refer [path]]

    [accountant.core :as accountant]
    [cljs.core.async :as async]
    [cljs.pprint :refer [pprint]]
    [reagent.core :as reagent]
    ))


(defn check-and-forward-to-singel-external [authentication-systems]
  (let [first-authsystem (first authentication-systems)]
    (if (and (= 1 (count authentication-systems))
             (= "external" (:type first-authsystem)))
      (external-authentication/request-authentication first-authsystem))))

(defonce authentication-systems* (reagent/atom nil))

(defn on-fetch-success [resp]
  (let [authentication-systems (->> resp :body
                                    (map-indexed
                                      #(assoc %2 :index %1 :key (:id %2))))]
    (check-and-forward-to-singel-external authentication-systems)
    (reset! authentication-systems* authentication-systems)))

(defn fetch-authentication-systems [& args]
  (defonce fetch-authentication-systems-id* (atom nil))
  (let [resp-chan (async/chan)
        p1 {:url (path :sign-in
                       {}
                       {:email (-> @routing/state* :query-params-raw :email)})
            :method :get}
        p2 {:modal false
            :title "Fetch authentication systems"
            :retry-fn #'fetch-authentication-systems}
        id (requests/send-off p1 p2 :chan resp-chan)]
    (reset! fetch-authentication-systems-id* id)
    (go (let [resp (<! resp-chan)]
          (when (= id @fetch-authentication-systems-id*)
            (case (:status resp)
              200 (on-fetch-success resp)
              (reset! authentication-systems* [])))))))

(defn reset-and-fetch []
  (reset! authentication-systems* nil)
  (fetch-authentication-systems))

(defn sign-in-failed []
  [:div
   [:div
    (when-not (nil? (-> @routing/state* :query-params-raw :email presence))
      [:div.alert.alert-danger
       "Signing in with this account is currently not possible! "
       "Check your email-address respectively login and try again. "
       "Contact your leihs administrator if the problem persists. "])]
   [sign-in/nav-email-continue-component path]])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn authentication-systems-component []
  [:div.authentication-systems
   (for [authentication-system  @authentication-systems*]
     (let [component (case (:type authentication-system)
                       "password" password-authentication/sign-in-component 
                       "external" external-authentication/sign-in-component)]
       [component authentication-system]))])

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div.debug
     [:hr]
     [:h3 "@authentication-systems*"]
     [:pre (with-out-str (pprint @authentication-systems*))]]))

(defn page []
  [:div.sign-in-page
   [routing/hidden-state-component
    {:did-change reset-and-fetch}]
   [:h1 "Sign in"]
   (cond
     (nil? @authentication-systems*) [:div.text-center
                                      [:i.fas.fa-spinner.fa-spin.fa-5x]
                                      [:span.sr-only "Please wait"]]
     (empty? @authentication-systems*) [sign-in-failed]
     :else [authentication-systems-component])
   [debug-component]])
