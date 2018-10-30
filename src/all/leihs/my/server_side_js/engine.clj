(ns leihs.my.server-side-js.engine
  (:require [aleph.flow :as flow]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [hiccup.core :refer [html]]
            [leihs.core.json :refer [to-json]])
  (:import [io.aleph.dirigiste Pools]
           [javax.script ScriptEngineManager Invocable]))

(defn- create-js-engine
  []
  (doto (.getEngineByName (ScriptEngineManager.) "nashorn")
    ; React requires either "window" or "global" to be defined.
    (.eval
      "window = this; global = this; // https://gist.github.com/josmardias/20493bd205e24e31c0a406472330515a
// at least one timeout needs to be set, larger then your code bootstrap
//  or Nashorn will run forever
// preferably, put a timeout 0 after your code bootstrap

(function(context) {
  'use strict';

  var Timer = Java.type('java.util.Timer');
  var Phaser = Java.type('java.util.concurrent.Phaser');

  var timer = new Timer('jsEventLoop', false);
  var phaser = new Phaser();

  var timeoutStack = 0;
  function pushTimeout() {
    timeoutStack++;
  }
  function popTimeout() {
    timeoutStack--;
    if (timeoutStack > 0) {
      return;
    }
    timer.cancel();
    phaser.forceTermination();
  }

  var onTaskFinished = function() {
    phaser.arriveAndDeregister();
  };

  context.setTimeout = function(fn, millis /* [, args...] */) {
    var args = [].slice.call(arguments, 2, arguments.length);

    var phase = phaser.register();
    var canceled = false;
    timer.schedule(function() {
      if (canceled) {
        return;
      }

      try {
        fn.apply(context, args);
      } catch (e) {
        print(e);
      } finally {
        onTaskFinished();
        popTimeout();
      }
    }, millis);

    pushTimeout();

    return function() {
      onTaskFinished();
      canceled = true;
      popTimeout();
    };
  };

  context.clearTimeout = function(cancel) {
    cancel();
  };

  context.setInterval = function(fn, delay /* [, args...] */) {
    var args = [].slice.call(arguments, 2, arguments.length);

    var cancel = null;

    var loop = function() {
      cancel = context.setTimeout(loop, delay);
      fn.apply(context, args);
    };

    cancel = context.setTimeout(loop, delay);
    return function() {
      cancel();
    };
  };

  context.clearInterval = function(cancel) {
    cancel();
  };

})(this);")
    (.eval (-> "public/server_side/bundle.js" ; path to file
               io/resource
               io/reader))))

; We have one and only one key in the pool, it's a constant.
(def ^:private js-engine-key "js-engine")
(def ^:private js-engine-pool
  (flow/instrumented-pool {:generate (fn [_] (create-js-engine)),
                           :controller
                             (Pools/utilizationController 0.9 10000 10000)}))

(defn render-react
  [name props]
  (let [js-engine @(flow/acquire js-engine-pool js-engine-key)]
    (try
      (.eval
        js-engine
        (str "renderComponentToStaticMarkup('" name "', " (to-json props) ")"))
      (finally (flow/release js-engine-pool js-engine-key js-engine)))))
