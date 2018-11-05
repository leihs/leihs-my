(ns leihs.my.remote-navbar.back
  (:require [leihs.my.back.ssr :refer [render-navbar]]))

(defn handler [request]
  {:body (render-navbar request)})
