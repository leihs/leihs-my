(defproject leihs-my "0.0.0"
  :description "My Service for Leihs"
  :url "https://github.com/leihs/leihs-my"
  :license {:name "AGPL"
            :url "https://www.gnu.org/licenses/agpl-3.0.de.html"}
  :dependencies
  [
   [aleph "0.4.6"]
   [bidi "2.1.3"]
   [buddy/buddy-sign "3.0.0"]
   [camel-snake-kebab "0.4.0"]
   [cheshire "5.8.0"]
   [clj-http "3.9.0"]
   [cljs-http "0.1.45"]
   [cljsjs/jimp "0.2.27"]
   [cljsjs/js-yaml "3.3.1-0"]
   [cljsjs/moment "2.22.2-0"]
   [clojure-humanize "0.2.2"]
   [com.github.mfornos/humanize-slim "1.2.2"]
   [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
   [compojure "1.6.1"]
   [environ "1.1.0"]
   [hiccup "1.0.5"]
   [hickory "0.7.1"]
   [hikari-cp "2.6.0"]
   [honeysql "0.9.3"]
   [inflections "0.13.0"]
   [io.dropwizard.metrics/metrics-core "4.0.3"]
   [io.dropwizard.metrics/metrics-healthchecks "4.0.3"]
   [io.forward/yaml "1.0.9"]
   [log4j/log4j "1.2.17" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
   [logbug "4.2.2"]
   [me.raynes/conch "0.8.0"]
   [nilenso/honeysql-postgres "0.2.4"]
   [org.clojure/clojure "1.9.0"]
   [org.clojure/clojurescript "1.10.339" :scope "provided"]
   [org.clojure/java.jdbc "0.7.7"]
   [org.clojure/tools.cli "0.3.7"]
   [org.clojure/tools.logging "0.4.1"]
   [org.clojure/tools.nrepl "0.2.13"]
   [org.slf4j/slf4j-log4j12 "1.7.25"]
   [pandect "0.6.1"]
   [pg-types "2.4.0-PRE.1"]
   [reagent "0.8.1"]
   [reagent-utils "0.3.1"]
   [ring "1.6.3"]
   [ring-middleware-accept "2.0.3"]
   [ring/ring-json "0.4.0"]
   [timothypratley/patchin "0.3.5"]
   [uritemplate-clj "1.2.1"]
   [venantius/accountant "0.2.4"]

   ; force transitive dependency resolution
   [ring/ring-core "1.6.3"]
   [com.google.guava/guava "22.0"]
   ]


  ; jdk 9 needs ["--add-modules" "java.xml.bind"]
  :jvm-opts #=(eval (if (re-matches #"^(9|10)\..*" (System/getProperty "java.version"))
                      ["--add-modules" "java.xml.bind"]
                      []))

  :javac-options ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]

  :source-paths ["src/all" "leihs-clj-shared/src"]

  :resource-paths ["resources/all"]

  :test-paths ["src/test"]

  :aot [#"leihs\..*"]

  :main leihs.my.back.main

  :plugins [[lein-asset-minifier "0.4.4" :exclusions [org.clojure/clojure]]
            [lein-cljsbuild "1.1.7"]
            [lein-environ "1.1.0"]
            [lein-shell "0.4.2"]]

  :cljsbuild {:builds
              {:min {:source-paths ["src/all" "src/prod" "leihs-clj-shared/src"]
                     :jar true
                     :compiler
                     {:output-to "target/cljsbuild/public/my/js/app.js"
                      :output-dir "target/uberjar"
                      :optimizations :simple
                      :pretty-print  false}}
               :app
               {:source-paths ["src/all" "src/dev" "leihs-clj-shared/src"]
                :compiler
                {:main "leihs.my.front.init"
                 :asset-path "/my/js/out"
                 :output-to "target/cljsbuild/public/my/js/app.js"
                 :output-dir "target/cljsbuild/public/my/js/out"
                 :source-map true
                 :optimizations :none
                 :pretty-print  true}}}}

  :figwheel {:http-server-root "public"
             :server-port 3231
             :nrepl-port 3232
             :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
             :css-dirs ["resources/all/public/my/css"]}

  :profiles {:dev
             {:dependencies [[com.cemerick/piggieback "0.2.2"]
                             [figwheel-sidecar "0.5.16"]
                             [org.clojure/tools.nrepl "0.2.13"]
                             [pjstadig/humane-test-output "0.8.3"]
                             [prone "1.6.0"]
                             [ring/ring-devel "1.6.3"]
                             [ring/ring-mock "0.3.2"]]
              :plugins [[lein-figwheel "0.5.16"]
                        [lein-sassy "1.0.8"]]
              :source-paths ["src/all" "src/dev" "leihs-clj-shared/src"]
              :resource-paths ["resources/all" "resources/dev" "target/cljsbuild"]
              :injections [(require 'pjstadig.humane-test-output)
                           (pjstadig.humane-test-output/activate!)]
              :env {:dev true}}
             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["src/all" "src/prod" "leihs-clj-shared/src"]
                       :prep-tasks [["shell" "./bin/build-timestamp"]
                                    "compile" ["cljsbuild" "once" "min"]]
                       :resource-paths ["resources/all" "resources/prod" "target/cljsbuild"]
                       :aot [#"leihs\..*"]
                       :uberjar-name "leihs-my.jar"}
             :test {:resource-paths ["resources/all" "resources/test" "target/cljsbuild"]}}


)
