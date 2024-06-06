(ns leihs.my.front.icons
  (:refer-clojure :exclude [next])
  (:require
   ["@fortawesome/free-solid-svg-icons" :as solids]
   ["@fortawesome/react-fontawesome" :as fa-react-fontawesome :refer [FontAwesomeIcon]]))

(defn user [] (FontAwesomeIcon #js{:icon solids/faUser :className ""}))
(defn password [] (FontAwesomeIcon #js{:icon solids/faKey :className ""}))
(defn user-in-admin [] (FontAwesomeIcon #js{:icon solids/faUserCog :className ""}))



