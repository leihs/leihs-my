(ns leihs.my.remote-navbar.back
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [hiccup.core :refer [html]]
            [leihs.my.server-side-js.engine :as js-engine]
            [leihs.core.sql :as sql]))


(defn handler
  [request]
    {:headers {"Content-Type" "text/plain"},
     :body (js-engine/render-react
             "Navbar"
             {:config {:appTitle "TODO",
                       :appColor "red"}})})
