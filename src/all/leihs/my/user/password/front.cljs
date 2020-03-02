(ns leihs.my.user.password.front
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    [cljs.core.async.macros :refer [go]])
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.requests.core :as requests]
    [leihs.core.routing.front :as routing]
    [leihs.core.breadcrumbs :as breadcrumbs]

    [leihs.my.front.breadcrumbs :as my-breadcrumbs]
    [leihs.my.front.state :as state]
    [leihs.my.paths :as paths :refer [path]]
    [leihs.my.user.password.breadcrumbs :as password-breadcrumbs]
    [leihs.my.user.shared :refer [me?*]]

    [accountant.core :as accountant]
    [cljs.core.async :as async]
    [cljs.core.async :refer [timeout]]
    [cljs.pprint :refer [pprint]]
    [clojure.contrib.inflect :refer [pluralize-noun]]
    [reagent.core :as reagent]
    ))

(def user-id*
  (reaction
    (-> @routing/state* :route-params :user-id)))

(def form-data* (reagent/atom {}))

(def form-data-valid*
  (reaction (-> @form-data* :password presence boolean)))

(defn submit [& args]
  (let [resp-chan (async/chan)
        id (requests/send-off {:url (path :password {:user-id @user-id*})
                               :method :put
                               :json-params @form-data*}
                              {:modal true
                               :title "Reset password"
                               :retry-fn #'submit}
                              :chan resp-chan)]
    (go (let [resp (<! resp-chan)]
          (when (= (:status resp) 204)
            (accountant/navigate!
              (path :my-user {:user-id @user-id*})))))))

(defn reset-data [& args]
  (reset! form-data* {}))

(defn page []
  [:div.password
   [routing/hidden-state-component
    {:did-mount reset-data
     :did-change reset-data}]
   (breadcrumbs/nav-component
     [(breadcrumbs/leihs-li)
      (my-breadcrumbs/user-li)
      (password-breadcrumbs/password-li)][])
   (if @me?*
     [:h1 "Reset My Password"]
     (let [id (-> @routing/state* :route-params :user-id)]
       [:div
        [:h1 "Reset User's Password"]
        [:p "user-id: " [:code id ]]]))
   [:p "You can set or reset the password here."]
   [:p "Note that a set password alone does not suffice to sign in via leihs password authentication. "
    "This also depends on some settings which can only be set by leihs administrators."]
   [:form.form.mt-2
    {:on-submit (fn [e]
                  (.preventDefault e)
                  (submit))}
    [:div.form-group
     [:label {:for :password} "New password:"]
     [:div
      [:input.form-control
       {:id :password
        :auto-complete :new-password
        :name :password
        :type :password
        :value (:password @form-data*)
        :on-change #(swap! form-data* assoc :password (-> % .-target .-value presence))}]]]
    [:div.form-group.float-right
     [:button.btn.btn-primary
      {:type :submit
       :disabled (not @form-data-valid*)}
      "Set password"]]
    [:div.clearfix]]])
