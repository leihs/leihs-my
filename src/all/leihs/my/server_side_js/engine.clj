(ns leihs.my.server-side-js.engine
  (:require
    [me.raynes.conch :refer [programs]]
    [clojure.tools.logging :as log]
    [leihs.core.json :refer [to-json]]))

(programs node)

(def render-wait-time-max-ms 5000)

(defn js-code
  [name props]
  (str
    "window = global = this
   l = require('./leihs-ui/dist/leihs-ui-server-side')
   l.renderComponentToString('"
    name
    "', "
    (to-json props)
    ")"))

(defn render-react
  [name props]
  (try
    (node "-p" (js-code name props) {:timeout render-wait-time-max-ms})
    (catch Exception e
      (throw (ex-info "Render Error!" {:status 500, :causes {:err e}})))))
