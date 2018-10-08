(ns leihs.my.sign-in.password-authentication.front
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

(def submit-password* (reagent/atom ""))

(defn authenticate [& args]
  (defonce authenticate-id* (atom nil))
  (let [resp-chan (async/chan)
        p1 {:url (path :password-authentication)
            :method :post
            :json-params {:email (-> @routing/state* :query-params-raw :email)
                          :password @submit-password*}}
        p2 {:modal false
            :title "Password Authentication"
            :retry-fn #'authenticate}
        id (requests/send-off p1 p2 :chan resp-chan)]
    (reset! authenticate-id* id)
    (go (let [resp (<! resp-chan)]
          (when (= id @authenticate-id*)
            (case (:status resp)
              200 (do (reset! user/state* (:body resp))
                      (routing/navigate! (path :home)))))))))


(defn sign-in-component [& [pws]]
  (let [pws (or pws {:id "password",
                     :type "password",
                     :name "leihs password",
                     :index 0,
                     :key "password"})
        password* (reagent/atom "")
        disabled?* (reaction (-> @password* presence boolean not))]
    (fn []
      [:div.password-authentication-system.my-4
       {:id (:id pws)}
       ; [:pre (with-out-str (pprint pws))]
       [:div.card
        [:div.card-header
         {:class (case (:index pws)
                   0 "text-white bg-primary"
                   "")}
         [:h2 [:i.fas.fa-key] " Sign in with \"" (:name pws) "\""]]
        [:div.card-body
         [:form.form
          {:on-submit (fn [e] (.preventDefault e)
                        (reset! submit-password* @password*)
                        (authenticate))}
          [:div.form-group.mx-sm-2.mb-2
           {:style {:display :none}}
           [:input#email
            {:type :email
             :value (-> @routing/state* :query-params-raw :email)
             :read-only true
             :auto-complete :email}]]
          [:div.form-group
           [:label "Password:"]
           [:input#password.form-control
            {:type :password
             :auto-complete :current-password
             :value @password*
             :on-change #(reset! password* (-> % .-target .-value))}]]
          [:div.form-group.float-right
           [:button.btn.mb-2
            {:disabled @disabled?*
             :class (if (= 0 (:index pws))
                      "btn-primary"
                      "btn-secondary")
             :type :submit}
            "Sign in"]]]]]])))

