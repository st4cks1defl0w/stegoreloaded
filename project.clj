(defproject com.stego/stego-reloaded "0.1.0-SNAPSHOT"
  :description "-"
  :url "gnu.org"
  :license {:name "GPL"
            :url "https://gnu.org/"}
  :resource-paths ["resources"] 
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [buddy/buddy-core "1.5.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [net.mikera/imagez "0.12.0"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [com.stuartsierra/component.repl "0.2.0"]]
                   :source-paths ["dev"]}})
