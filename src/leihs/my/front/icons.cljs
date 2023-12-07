(ns leihs.my.front.icons
  (:refer-clojure :exclude [next])
  (:require
   ["@fortawesome/free-brands-svg-icons" :as brands]
   ["@fortawesome/free-solid-svg-icons" :as solids]
   ["@fortawesome/react-fontawesome" :as fa-react-fontawesome :refer [FontAwesomeIcon]]
   [leihs.core.icons :as core-icons]))

(def delete core-icons/delete)

(defn add [] (FontAwesomeIcon #js{:icon solids/faPlusCircle :className ""}))
(defn api-token [] (FontAwesomeIcon #js{:icon solids/faCoins :className ""}))
(defn api-tokens [] [api-token])
(defn edit [] (FontAwesomeIcon #js{:icon solids/faEdit :className ""}))
(defn user [] (FontAwesomeIcon #js{:icon solids/faUser :className ""}))
(defn password [] (FontAwesomeIcon #js{:icon solids/faKey :className ""}))
(defn user-in-admin [] (FontAwesomeIcon #js{:icon solids/faUserCog :className ""}))



