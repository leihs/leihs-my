(load-file "shared-clj/deps.clj")

(defproject leihs-my "0.0.0"
  :description "My Service for Leihs"
  :url "https://github.com/leihs/leihs-my"
  :license {:name "AGPL"
            :url "https://www.gnu.org/licenses/agpl-3.0.de.html"}
  :dependencies ~(extend-shared-deps '[[clj-ulid "0.1.0-SNAPSHOT"]])

  ; jdk 9 needs ["--add-modules" "java.xml.bind"]
  :jvm-opts #=(eval (if (re-matches #"^(9|10)\..*" (System/getProperty "java.version"))
                      ["--add-modules" "java.xml.bind"]
                      []))

  :javac-options ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]

  :source-paths ["src/all" "shared-clj/src"]

  :resource-paths ["resources/all"]

  :test-paths ["src/test"]

  :aot [#"leihs\..*"]

  :main leihs.my.back.main

  :plugins [[lein-asset-minifier "0.4.4" :exclusions [org.clojure/clojure]]
            [lein-cljsbuild "1.1.7"]
            [lein-environ "1.1.0"]
            [lein-shell "0.4.2"]]

  :aliases {"auto-reset" ["auto" "exec" "-p" "scripts/lein-exec-reset.clj"]}

  :cljsbuild {:builds
              {:min {:source-paths ["src/all" "src/prod" "shared-clj/src"]
                     :jar true
                     :compiler
                     {:output-to "target/cljsbuild/public/my/js/app.js"
                      :output-dir "target/uberjar"
                      :optimizations :simple
                      :pretty-print  false}}
               :app
               {:source-paths ["src/all" "src/dev" "shared-clj/src"]
                :compiler
                {:main "leihs.my.front.init"
                 :asset-path "/my/js/out"
                 :output-to "target/cljsbuild/public/my/js/app.js"
                 :output-dir "target/cljsbuild/public/my/js/out"
                 :source-map true
                 :optimizations :none
                 :pretty-print  true}}}}

  :figwheel {:http-server-root "public"
             :server-port 3241
             :nrepl-port 3232
             :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
             :css-dirs ["resources/all/public/my/css"]}

  :profiles {:dev-common
             {:dependencies [[com.cemerick/piggieback "0.2.2"]
                             [figwheel-sidecar "0.5.16"]
                             [nrepl "0.6.0"]
                             [org.clojure/tools.namespace "0.2.11"]
                             [pjstadig/humane-test-output "0.8.3"]
                             [prone "1.6.0"]
                             [ring/ring-devel "1.6.3"]
                             [ring/ring-mock "0.3.2"]]
              :plugins [[lein-auto "0.1.3"]
                        [lein-exec "0.3.7"]
                        [lein-figwheel "0.5.16"]
                        [lein-sassy "1.0.8"]]
              :source-paths ["src/all" "src/dev" "shared-clj/src"]
              :resource-paths ["resources/all" "resources/dev" "target/cljsbuild"]
              :injections [(require 'pjstadig.humane-test-output)
                           (pjstadig.humane-test-output/activate!)]
              :env {:dev true}}
             :dev-overrides {} ; defined if needed in profiles.clj file
             :dev [:dev-common :dev-overrides]
             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["src/all" "src/prod" "shared-clj/src"]
                       :prep-tasks [["shell" "./bin/build-timestamp"]
                                    "compile" ["cljsbuild" "once" "min"]]
                       :resource-paths ["resources/all" "resources/prod" "target/cljsbuild"]
                       :aot [#"leihs\..*"]
                       :uberjar-name "leihs-my.jar"}
             :test {:resource-paths ["resources/all" "resources/test" "target/cljsbuild"]}})
