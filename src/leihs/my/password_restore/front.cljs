(ns leihs.my.password-restore.front
  (:refer-clojure :exclude [str keyword])
  (:require
   [leihs.core.constants]
   [leihs.core.dom :as dom]
   ["/my-ui" :as UI]))


(defn forgot-password []
  (let [page-props (dom/data-attribute "body" "page-props")]
    (fn []
      (if (-> page-props :status (= "success"))
        [:> UI/Components.PasswordForgotSuccessPage page-props]
        [:> UI/Components.PasswordForgotPage page-props]))))

(defn reset-password []
  (let [page-props (dom/data-attribute "body" "page-props")]
    (fn []
      (if (-> page-props :status (= "success"))
        [:> UI/Components.PasswordResetSuccessPage page-props]
        [:> UI/Components.PasswordResetPage page-props]))))
