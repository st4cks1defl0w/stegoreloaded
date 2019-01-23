(defproject stegoreloaded "0.1"
  :description "Tiny program to encrypt info in pixels of a .png image"
  :url "gnu.org"
  :license {:name "GPL"
            :url "https://gnu.org/"}
  :resource-paths ["resources"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [buddy/buddy-core "1.5.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [net.mikera/imagez "0.12.0"]
                 [org.clojure/tools.cli "0.4.1"]]
  :main ^:skip-aot stegoreloaded.core
 ;;:aot [stegoreloaded.core]
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [com.stuartsierra/component.repl "0.2.0"]]
                   :source-paths ["dev"]
                   :plugins [[lein-bikeshed "0.5.1"]
                            [lein-cljfmt "0.6.3"]]}})
