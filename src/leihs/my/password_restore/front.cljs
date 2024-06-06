(ns leihs.my.password-restore.front
  (:require
   ["/my-ui" :as UI]
   [leihs.core.constants]
   [leihs.core.dom :as dom]))

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
