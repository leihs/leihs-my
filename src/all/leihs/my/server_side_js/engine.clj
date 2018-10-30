(ns leihs.my.server-side-js.engine
  (:require [aleph.flow :as flow]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [hiccup.core :refer [html]])
  (:import [io.aleph.dirigiste Pools]
           [javax.script ScriptEngineManager Invocable]))

(defn- create-js-engine []
  (doto (.getEngineByName (ScriptEngineManager.) "nashorn")
    ; React requires either "window" or "global" to be defined.
    (.eval "var global = this")
    (.eval (-> "public/server_side/file.js" ; path to file
               io/resource
               io/reader))))

; We have one and only one key in the pool, it's a constant.
(def ^:private js-engine-key "js-engine")
(def ^:private js-engine-pool
  (flow/instrumented-pool
    {:generate   (fn [_] (create-js-engine))
     :controller (Pools/utilizationController 0.9 10000 10000)}))

(defn invoke [func & args]
  (let [js-engine @(flow/acquire js-engine-pool js-engine-key)]
    (try (.invokeFunction js-engine func (object-array args))
         (finally (flow/release js-engine-pool js-engine-key js-engine)))))
