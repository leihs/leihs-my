(ns leihs.my.server-side-js.engine
  (:require [clojure.tools.logging :as log]
            [clojure.java.shell :refer [sh]]
            [leihs.core.json :refer [to-json]]))

(defn render-react
  [name props]
  (let
    [js
       (str
         "window = global = this
          l = require('./leihs-ui/dist/leihs-ui-server-side')
          l.renderComponentToString('"
         name
         "', "
         (to-json props)
         ")")]
    (:out (sh "node" "-p" js))))
