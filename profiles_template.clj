; Copy this file to profiles.clj, if you intend to use the auto watch/reset.

{:dev-overrides {:aot ^:replace [],
                 :dependencies [[org.clojure/tools.namespace "0.2.11"]],
                 :repl-options {:init (require 'app)}}}
