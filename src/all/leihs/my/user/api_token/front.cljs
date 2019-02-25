(ns leihs.my.user.api-token.front
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
    [leihs.my.front.shared :refer [humanize-datetime-component]]
    [leihs.my.front.state :as state]
    [leihs.my.paths :as paths :refer [path]]
    [leihs.my.user.api-tokens.breadcrumbs :as api-tokens-breadcrumbs]
    [leihs.my.user.shared :refer [me?*]]

    [accountant.core :as accountant]
    [cljs.core.async :as async]
    [cljs.core.async :refer [timeout]]
    [cljs.pprint :refer [pprint]]
    [cljsjs.moment]
    [clojure.string :refer [join split]]
    [reagent.core :as reagent]

    ))


(defonce user-id* (reaction (-> @routing/state* :route-params :user-id)))
(defonce api-token-id* (reaction (-> @routing/state* :route-params :api-token-id)))
(defonce api-token-data* (reagent/atom nil))

(defonce mode?*
  (reaction
    (case (:handler-key @routing/state*)
      :api-token-add :add
      (:api-token :api-token-delete) :show
      :api-token-edit :edit
      nil
      )))


(defonce edit-mode?*
  (reaction
    (and (map? @api-token-data*)
         (boolean ((set '(:api-token-edit :api-token-add))
                   (:handler-key @routing/state*))))))

(defn valid-iso8601? [iso]
  (.isValid (js/moment iso)))


(def description-valid*? (reaction (-> @api-token-data* :description presence boolean)))

(def expires-at-valid*? (reaction (-> @api-token-data* :expires_at valid-iso8601?)))

(def form-valid*? (reaction (and @description-valid*?
                                 expires-at-valid*?)))

(defn reset-api-token-api-token-data []
  (reset! api-token-data*
          {:description ""
           :scope_read true
           :scope_write false
           :scope_admin_read false
           :scope_admin_write false
           :expires_at (.format (.add (js/moment) 1, "year"))
           }))

(declare add patch delete)

;;; form scopes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def scopes
  (->> [:scope_read :scope_write 
        :scope_admin_read :scope_admin_write 
        :scope_system_admin_read :scope_system_admin_write]
       (map (fn [x] {:scope x :key x}))))

(defn scope-disalbed? [scope]
  (or (= :show @mode?*)
      (case scope
        :scope_read (or (-> @api-token-data* :scope_admin_read)
                        (-> @api-token-data* :scope_system_admin_read)
                        (-> @api-token-data* :scope_write))
        :scope_write (or (-> @api-token-data* :scope_read not)
                         (-> @api-token-data* :scope_admin_write)
                         (-> @api-token-data* :scope_system_admin_write))
        :scope_admin_read (or (-> @api-token-data* :scope_read not)
                              (-> @api-token-data* :scope_admin_write))
        :scope_admin_write (or (-> @api-token-data* :scope_admin_read not)
                               (-> @api-token-data* :scope_write not))
        :scope_system_admin_read (or (-> @api-token-data* :scope_read not)
                              (-> @api-token-data* :scope_system_admin_write))
        :scope_system_admin_write (or (-> @api-token-data* :scope_system_admin_read not)
                               (-> @api-token-data* :scope_write not)))))

(defn scope-text [scope]
  [:span
   (->> (-> scope str (split "_")) (drop 1) (join " "))])

(defn scope-form-component [{scope :scope}]
  [:div.checkbox
   {:key (str scope)}
   [:label
    [:input {:id (str scope)
             :type :checkbox
             :disabled (scope-disalbed? scope)
             :checked (-> @api-token-data* scope boolean)
             :on-change #(swap! api-token-data* assoc scope (-> @api-token-data* scope presence not))}]
    [:span
     {:class (if (scope-disalbed? scope) "text-muted" "")}
     " "
     [scope-text scope]
     ]]])

(defn scopes-form-component []
  [:div.form-group
   [:label [:b "Scope and actions:" ]]
   (doall (for [scope scopes] [scope-form-component scope]))
   (when-not (= :show @mode?*)
     [:small.form-text
      {:class (when-not (-> @api-token-data* :scope_read presence boolean) "text-warning")}
      "Not setting at least \"read\" will practically disable this token!" ]
     [:small.form-text
      "Read and write correspond to perform actions
      via safe (read) or unsafe (write) HTTP verbs." ]
     [:small.form-text
      "Enabled admin scopes will have effect if and only if the corresponding
      user has admin privileges at the time this tokes is used."])])

;;; form description ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn description-form-component []
  [:div.form-group
   [:label {:for :description} [:b "Description:" ]]
   [:input#description.form-control
    {:class (when (not @description-valid*?) "is-invalid")
     :on-change #(swap! api-token-data* assoc :description (-> % .-target .-value presence))
     :value (-> @api-token-data* :description)
     :disabled (= @mode?* :show)}]
   (when (#{:add :edit} @mode?*)
     [:small.form-text
      {:class (if (not @description-valid*?) "text-danger" "text-muted")}
      "The description may not be empty!" ])])

;;; form expires ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn expires-presets-component []
  [:small.form-text "Presets: "
   (for [period ["day" "week" "month" "year"]]
     [:span {:key period} " "
      [:a.btn.btn-sm.btn-outline-secondary
       {:id period
        :href "#"
        :on-click #(swap! api-token-data* assoc
                     :expires_at (->  (.add (js/moment) 1, period) .format))}
       "now + 1 " period]])
   [:span {:key :never} " "
    [:a.btn.btn-sm.btn-outline-secondary
     {:id :never
      :href "#"
      :on-click #(swap! api-token-data* assoc
                   :expires_at (->  (.add (js/moment) 1000, "years") .format))}
     "never"]]])

(defn expires-at-form-component []
  [:div.form-group {:class (when (not @expires-at-valid*?) "has-error")}
   [:label {:for :expires_at} [:b "Expires" ":" ]]
   [:input#expires_at.form-control
    {:class (when (not @expires-at-valid*?) "is-invalid")
     :type :datetime-local
     :on-change #(when-let [iso (-> % .-target .-value presence
                                    (js/moment "YYYY-MM-DDTHH:mm:ss") .format)]
                   (swap! api-token-data* assoc :expires_at iso))
     :value (-> @api-token-data* :expires_at
                js/moment .local (.format "YYYY-MM-DDTHH:mm:ss"))
     :disabled (= :show @mode?*)}]

   [:small.form-text
    (if @expires-at-valid*?
      [:span.text-muted "This API-token will expire "
       (humanize-datetime-component (-> @api-token-data* :expires_at js/moment))
       ". "]
      [:span.text-danger
       "A valid expiration date must be set!"])]
   (when-not (= :show @mode?*)
     [expires-presets-component])])


;;; form timestamps ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn form-timestamps-component []
  (when (#{:edit :show} @mode?*)
    [:div.form-group
     [:p.form-text
      "This token has been created "
      (-> @api-token-data* :created_at humanize-datetime-component)
      ", and updated "
      (-> @api-token-data* :updated_at humanize-datetime-component) ". "]]))


;;; submit ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare add-button-component
         delete-button-component
         edit-button-component)

(defn submit-component []
  [:div
   [:div.float-right
    (case (:handler-key @routing/state*)
      :api-token-add [add-button-component]
      :api-token-delete [delete-button-component]
      :api-token-edit [edit-button-component]
      nil)]
   [:div.clearfix]])

;;; form main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn form-component []
  [:form.form
   {:on-submit (fn [e]
                 (.preventDefault e)
                 (case (:handler-key @routing/state*)
                   :api-token-add (add)
                   :api-token-edit (patch)
                   :api-token-delete (delete)
                   ))}
   [description-form-component]
   [scopes-form-component]
   [expires-at-form-component]
   [form-timestamps-component]
   [submit-component]])

;;; new page ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def token-secret* (reaction (-> @api-token-data* :token_secret)))

(defn add [& args]
  (let [resp-chan (async/chan)
        id (requests/send-off {:url (path :api-tokens {:user-id @user-id*})
                               :method :post
                               :json-params  @api-token-data*}
                              {:modal true
                               :title "Add API-Token"
                               :retry-fn #'add}
                              :chan resp-chan)]
    (go (let [resp (<! resp-chan)]
          (when (= (:status resp) 200)
            (reset! api-token-data* (-> resp :body)))))))

(defn add-button-component []
  [:button.btn.btn-primary
   {:disabled (not @form-valid*?)}
   " Add "])

(defn debug-component []
  (when (:debug @state/global-state*)
    [:div.debug
     [:hr]
     [:div
      [:h3 "@api-token-data*"]
      [:pre (with-out-str (pprint @api-token-data*))]]
     [:div
      [:h3 "@mode?*"]
      [:pre (with-out-str (pprint @mode?*))]]
     [:div
      [:h3 "@description-valid*?"]
      [:pre (with-out-str (pprint @description-valid*?))]]
    [:div
      [:h3 "@form-valid*?"]
      [:pre (with-out-str (pprint @form-valid*?))]] ]))

(defn reset-api-token-form-data []
  (reset! api-token-data*
          {:description nil
           :scope_read true
           :scope_write true
           :scope_admin_read true
           :scope_admin_write true
           :expires_at (.format (.add (js/moment) 1, "year"))
           }))

(defn new-token-secret-modal []
  (when @token-secret*
    [:div {:style {:opacity "1.0" :z-index 10000}}
     [:div.modal {:style {:display "block" :z-index 10000}}
      [:div.modal-dialog
       [:div.modal-content
        [:div.modal-header.text-success
         [:h4 "The new API-Token "
          [:code.token_part (:token_part @api-token-data*)]
          " has been added"]]
        [:div.modal-body.bg-warning
         [:h4.text-center
          [:code.token_secret @token-secret*]]
         [:p
          "The full token-secret is shown here once and only once. "
          "Only the first 5 letters will be stored and shown as a identifier. " ]]
        [:div.modal-footer
         [:button.btn.btn-primary
          {:on-click #(accountant/navigate!
                        (path :api-tokens {:user-id @user-id*}))}
          " Continue "]]]]]
     [:div.modal-backdrop {:style {:opacity "0.5"}}]]))

(defn add-page []
  [:div.new-api-token
   [new-token-secret-modal]
   [routing/hidden-state-component
    {:will-mount (fn [] (reset-api-token-form-data))
     :will-unmount #(reset! api-token-data* nil)}]
   (breadcrumbs/nav-component
     [(breadcrumbs/leihs-li)
      (my-breadcrumbs/user-li)
      (api-tokens-breadcrumbs/api-tokens-li)
      (api-tokens-breadcrumbs/api-token-add-li)]
     [])
   [:div
    (if @me?*
      [:h1 "Add My API-Token "]
      (let [id (-> @routing/state* :route-params :user-id)]
        [:div 
         [:h1 "Add User's API-Token"]
         [:p "user-id: " [:code id ]]]))
    [form-component]
    [debug-component]]])


;;; show page ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def fetch-token-id* (reagent/atom nil))

(defn fetch-token [& args]
  (let [resp-chan (async/chan)
        id (requests/send-off {:url (path :api-token {:user-id @user-id* :api-token-id @api-token-id*})
                               :method :get}
                              {:modal false
                               :title "Fetch Api-Tokens"
                               :handler-key :api-token
                               :retry-fn #'fetch-token}
                              :chan resp-chan)]
    (reset! fetch-token-id* id)
    (go (let [resp (<! resp-chan)]
          (when (and (= (:status resp) 200) ;success
                     (= id @fetch-token-id*) ;still the most recent request
                     (reset! api-token-data* (->> resp :body))))))))

(defn show-page []
  [:div.api-token
   [routing/hidden-state-component
    {:will-mount (fn [] (reset! api-token-data* nil))
     :did-change (fn [old diff new]
                   (js/console.log (with-out-str (pprint  diff)))
                   (fetch-token))
     :did-mount fetch-token}]
   (breadcrumbs/nav-component
     [(breadcrumbs/leihs-li)
      (my-breadcrumbs/user-li)
      (api-tokens-breadcrumbs/api-tokens-li)
      (api-tokens-breadcrumbs/api-token-li)] 
     [(api-tokens-breadcrumbs/api-token-delete-li)
      (api-tokens-breadcrumbs/api-token-edit-li)
      ])
   [:div
    (let [part (:token_part @api-token-data*)]
      (if @me?*
        [:h1 "My API-Token " [:code part]]
        (let [id (-> @routing/state* :route-params :user-id)]
          [:div 
           [:h1 "User's API-Token" [:code part]]
           [:p "user-id: " [:code id ]]])))]
   (if @api-token-data*
     [form-component]
     [:div.text-center
      [:i.fas.fa-spinner.fa-spin.fa-5x]])
   [debug-component]
   ])


;;; edit ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn patch [& args]
  (let [resp-chan (async/chan)
        id (requests/send-off {:url (path :api-token
                                          {:user-id @user-id*
                                           :api-token-id @api-token-id*})
                               :method :patch
                               :json-params  @api-token-data*}
                              {:modal true
                               :title "Update API-Token"
                               :handler-key :api-token-edit
                               :retry-fn #'patch}
                              :chan resp-chan)]
    (go (let [resp (<! resp-chan)]
          (when (= (:status resp) 204)
            (accountant/navigate!
              (path :api-token {:api-token-id @api-token-id*
                                :user-id @user-id*})))))))

(defn edit-button-component []
  [:button.btn.btn-warning
   {:disabled (not @form-valid*?)}
   [:i.fas.fa-save]
   " Save "])

(defn edit-page []
  [:div.api-token
   [routing/hidden-state-component
    {:did-change fetch-token}]
   (breadcrumbs/nav-component
     [(breadcrumbs/leihs-li)
      (my-breadcrumbs/user-li)
      (api-tokens-breadcrumbs/api-tokens-li)
      (api-tokens-breadcrumbs/api-token-li)
      (api-tokens-breadcrumbs/api-token-edit-li)]
     [])
   [:div
    (let [part (:token_part @api-token-data*)]
      (if @me?*
        [:h1 "Edit My API-Token " [:code part]]
        (let [id (-> @routing/state* :route-params :user-id)]
          [:div 
           [:h1 "Edit User's API-Token" [:code part]]
           [:p "user-id: " [:code id ]]])))]
   (if @api-token-data*
     [form-component]
     [:div.text-center
      [:i.fas.fa-spinner.fa-spin.fa-5x]])
   [debug-component]])


;;; delete ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn delete [& args]
  (let [resp-chan (async/chan)
        id (requests/send-off {:url (path :api-token
                                          {:user-id @user-id*
                                           :api-token-id @api-token-id*})
                               :method :delete
                               :query-params {}}
                              {:title "Delete API-Token"
                               :handler-key :api-token-delete
                               :retry-fn #'delete}
                              :chan resp-chan)]
    (go (let [resp (<! resp-chan)]
          (when (= (:status resp) 204)
            (accountant/navigate!
              (path :api-tokens {:user-id @user-id*}
                    )))))))

(defn delete-button-component []
  [:button.btn.btn-danger
   {:disabled (not @form-valid*?)}
   [:i.fas.fa-times]
   " Delete "])

(defn delete-page []
  [:div.api-token
   [routing/hidden-state-component
    {:will-mount (fn [] (reset! api-token-data* nil))
     :did-change fetch-token}]
   (breadcrumbs/nav-component
     [(breadcrumbs/leihs-li)
      (my-breadcrumbs/user-li)
      (api-tokens-breadcrumbs/api-tokens-li)
      (api-tokens-breadcrumbs/api-token-li)
      (api-tokens-breadcrumbs/api-token-delete-li)]
     [])
   (let [part (:token_part @api-token-data*)]
     (if @me?*
       [:h1 "Delete My API-Token " [:code part]]
       (let [id (-> @routing/state* :route-params :user-id)]
         [:div 
          [:h1 "Delete User's API-Token" [:code part]]
          [:p "user-id: " [:code id ]]])))
   (if @api-token-data*
     [form-component]
     [:div.text-center
      [:i.fas.fa-spinner.fa-spin.fa-5x]])
   [debug-component]])
