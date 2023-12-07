(ns leihs.my.initial-admin.front
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [accountant.core :as accountant]
   [cljs.core.async :as async]
   [cljs.core.async :refer [timeout]]
   [cljs.pprint :refer [pprint]]

   [leihs.core.anti-csrf.front :as anti-csrf]
   [leihs.core.breadcrumbs :as breadcrumbs]
   [leihs.core.core :refer [keyword str presence]]
   [leihs.core.requests.core :as requests]

   [leihs.my.front.components :as components]
   [leihs.my.front.shared :refer [humanize-datetime-component short-id gravatar-url]]
   [leihs.my.front.state :as state]
   [leihs.my.paths :as paths :refer [path]]
   [reagent.core :as reagent]))

(def form-data* (reagent/atom {}))

(def text-input-fields [:firstname :lastname])

(def email-valid*?
  (reaction
   (boolean
    (when-let [email (-> @form-data* :email presence)]
      (re-matches #".+@.+" email)))))

(def password-valid*?
  (reaction
   (boolean (-> @form-data* :password presence))))

(def form-valid*? (reaction (and @email-valid*? @password-valid*?)))

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div.debug
     [:hr]
     [:h3 "@form-data*"]
     [:pre (with-out-str (pprint @form-data*))]]))

(defn text-input-component
  ([kw]
   (text-input-component kw {}))
  ([kw opts]
   (let [opts (merge {:type :text
                      :valid* (reaction (-> @form-data* kw presence))}
                     opts)]
     [:div.form-group
      {:key kw}
      [:label {:for kw} kw]
      [:input.form-control
       {:type (:type opts)
        :class (if @(:valid* opts) "" "is-invalid")
        :value (or (-> @form-data* kw) "")
        :on-change #(swap! form-data* assoc kw (-> % .-target .-value presence))}]])))

(defn form-component []
  [:form#initial-admin-form.form
   {:method :post
    :action (path :initial-admin)}
   [anti-csrf/hidden-form-group-token-component]
   [:div.form-group
    [:label {:for :email} "email "]
    [:div
     [:input.form-control
      {:id :initial-admin-email
       :class (when-not @email-valid*? "is-invalid")
       :auto-complete :email
       :name :email
       :type :email
       :value (:email @form-data*)
       :on-change #(swap! form-data* assoc :email (-> % .-target .-value presence))}]]]
   [:div.form-group
    [:label {:for :password} "password"]
    [:div
     [:input.form-control
      {:id :password
       :class (when-not @email-valid*? "is-invalid")
       :name :password
       :type :password
       :value (:password @form-data*)
       :on-change #(swap! form-data* assoc :password (-> % .-target .-value presence))}]]]

   [:div.form-group.float-right
    [:button.btn.btn-primary
     {:type :submit
      :disabled (not @form-valid*?)}
     "Create initial adminstrator"]]
   [:div.clearfix]])

(defn page []
  [:div.initial-admin
   [:div.row
    [:nav.col-lg {:aria-label :breadcrumb :role :navigation}
     [:ol.breadcrumb
      [breadcrumbs/leihs-li]
      [breadcrumbs/admin-li]
      [breadcrumbs/initial-admin-li]]]
    [:nav.col-lg {:role :navigation}]]
   [:div
    [:h1 "Initial Admin"]
    [:p "An initial administrator account is required to sign in and further configure this instance of leihs."]
    [form-component]]])
