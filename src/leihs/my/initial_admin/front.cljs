(ns leihs.my.initial-admin.front
  (:require
   [leihs.core.anti-csrf.front :as anti-csrf]
   [leihs.core.core :refer [presence]]
   [leihs.my.paths :as paths :refer [path]]
   [reagent.core :as reagent :refer [reaction]]))

(def form-data* (reagent/atom {}))

(def email-valid*?
  (reaction
   (boolean
    (when-let [email (-> @form-data* :email presence)]
      (re-matches #".+@.+" email)))))

(def password-valid*?
  (reaction
   (boolean (-> @form-data* :password presence))))

(def form-valid*? (reaction (and @email-valid*? @password-valid*?)))

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

   [:div.form-group
    [:button.btn.btn-primary
     {:type :submit
      :disabled (not @form-valid*?)}
     "Create initial administrator"]]
   [:div.clearfix]])

(defn page []
  [:div.initial-admin
   [:div {:style {:max-width "500px"}}
    [:h1 "Initial Admin"]
    [:p "An initial administrator account is required to sign in and further configure this instance of leihs."]
    [form-component]]])
