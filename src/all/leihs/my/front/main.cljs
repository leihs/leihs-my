(ns leihs.my.front.main
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    )
  (:require
    [leihs.my.front.html :as html]
    [leihs.my.routes :as routes]
    [leihs.my.paths :as paths]
    [leihs.core.core :refer [keyword str presence]]

    [clojure.string :as str]
    [clojure.pprint :refer [pprint]]

    [reagent.core :as reagent]
    [accountant.core :as accountant]
    ))

(defn init! []
  (routes/init)
  (html/mount))
