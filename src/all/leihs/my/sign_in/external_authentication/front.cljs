(ns leihs.my.sign-in.external-authentication.front
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.breadcrumbs :as breadcrumbs]
    [leihs.core.constants]
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.requests.core :as requests]
    [leihs.core.routing.front :as routing]
    [leihs.core.user.front :as user]

    [leihs.my.front.state :as state]
    [leihs.my.paths :as paths :refer [path]]

    [cljs.core.async :as async]
    [cljs.core.async :refer [timeout]]
    [cljs.pprint :refer [pprint]]
    [reagent.cookies :as cookies]
    [reagent.core :as reagent]
    ))

(defn request-authentication [authentication-system-id]
  (defonce request-authentication-id* (atom nil))
  (let [resp-chan (async/chan)
        p1 {:url (path :external-authentication-request
                       {:authentication-system-id authentication-system-id})
            :method :post
            :json-params {:user-unique-id (-> @routing/state* :query-params-raw :email)}}
        p2 {:modal false
            :title "External Authentication"}
        id (requests/send-off p1 p2 :chan resp-chan)]
    (reset! request-authentication-id* id)
    (go (let [resp (<! resp-chan)]
          (when (= id @request-authentication-id*)
            (case (:status resp)
              200 (let [url (str (-> resp :body :url)
                                 "?token=" (-> resp :body :token))]
                    (set! (.-href (.-location js/window)) url))))))))

(defn sign-in-component [& [pws]]
  (fn []
    [:div.external-authentication-system.my-4
     {:id (:id pws)}
     ; [:pre (with-out-str (pprint pws))]
     [:div.card
      [:div.card-header
       {:class (case (:index pws)
                 0 "text-white bg-primary"
                 "")}
       [:h2 [:i.fas.fa-external-link-alt] " Sign via external service \"" (:name pws) "\""]]
      [:div.card-body
       [:form.form
        {:on-submit (fn [e] 
                      (.preventDefault e)
                      (request-authentication (:id pws))
                      )}
        [:div.form-group.float-right
         [:button.btn
          {:class (if (= 0 (:index pws))
                    "btn-primary"
                    "btn-secondary")
           :type :submit}
          "Continue"]]
        [:div.clearfix]]]]]))


;;;;

(def authentication-system-id* (reaction (-> @routing/state* :route-params :authentication-system-id)))

(def sign-in-error* (reagent/atom nil))

(defn sign-in []
  (defonce sign-in-id* (atom nil))
  (let [resp-chan (async/chan)
        p1 {:url (-> @routing/state* :url)
            :method :post}
        p2 {:modal false
            :title "Sign in via External Authentication"}
        id (requests/send-off p1 p2 :chan resp-chan)]
    (reset! sign-in-id* id)
    (go (let [resp (<! resp-chan)]
          (requests/dismiss id)
          (when (= id @sign-in-id*)
            (case (:status resp)
              200 (do (reset! user/state* (:body resp))
                      (routing/navigate! (path :home)))
              (reset! sign-in-error* resp)))))))


(defn on-load []
  (reset! sign-in-error* nil)
  (sign-in))

(defn sign-in-page []
  [:div.sign-in-page
   [routing/hidden-state-component
    {:did-change on-load}]
   [:h1 "Signing in via \"" 
    (-> @routing/state* :route-params :authentication-system-id)
    "\""]
   (when @sign-in-error*
     [:div.alert.alert-danger
      [:p [:strong "Authentication failed!"]
       " Please try again or contact your leihs administrator if the problem persists. "]
      (when-let [body (-> @sign-in-error* :body presence)]
        [:div
         [:p "The following has been sent with the reponse: "]
         [:pre (with-out-str (pprint body))]])])])
