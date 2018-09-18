(ns leihs.my.routes
  (:refer-clojure :exclude [str keyword])
  (:require-macros
    [reagent.ratom :as ratom :refer [reaction]]
    )
  (:require
    [leihs.core.core :refer [keyword str presence]]
    [leihs.core.routing.front :as routing]

    [leihs.my.front.state]
    [leihs.my.initial-admin.front :as initial-admin]
    [leihs.my.paths :as paths :refer [path paths]]
    [leihs.my.resources.home.front :as home]
    [leihs.my.resources.status.front :as status]
    [leihs.my.sign-in.front :as sign-in]

    [cljsjs.js-yaml]
    [clojure.pprint :refer [pprint]]
    [reagent.core :as reagent]
    ))

(def resolve-table
  {:home #'home/page
   :initial-admin #'initial-admin/page
   :sign-in #'sign-in/page
   :status #'status/info-page})


(defn init []
  (routing/init paths resolve-table paths/external-handlers))
