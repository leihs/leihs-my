(ns leihs.my.remote-navbar.back
  (:require
    [hiccup.core :refer [html]]
    [hiccup.page :refer [include-js]]
    [leihs.core.http-cache-buster2 :as cache-buster]
    [leihs.my.back.ssr :refer [render-navbar]]))

(defn handler [request]
  {:body (str (render-navbar request)
              (-> "/my/leihs-shared-bundle.js"
                  cache-buster/cache-busted-path
                  include-js
                  html))})
