(ns ^:figwheel-no-load leihs.my.front.init
  (:require
    [leihs.my.front.main]
    [leihs.my.front.html]
    [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3241/figwheel-ws"
  :jsload-callback leihs.my.front.html/mount)

(leihs.my.front.main/init!)
