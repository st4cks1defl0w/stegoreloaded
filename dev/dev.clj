(ns dev
  "Tools for interactive development with the REPL."
  (:require
   [clojure.java.io :as io]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint :refer [pprint]]
   [clojure.reflect :refer [reflect]]
   [clojure.repl :refer [apropos dir doc find-doc pst source]]
   [clojure.set :as set]
   [clojure.string :as string]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :refer [refresh refresh-all clear]]
   [com.stuartsierra.component :as component]
   [com.stuartsierra.component.repl :refer [reset set-init start stop system]]
   [com.stego.stego-reloaded :as stego]))

(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src" "test")

(def replable-configs {:startingimg "hothead.png"
                       :message "im a hidden bugger"
                       :password "secretpassword"
                       :decrypt true})

(defn example-system [config]
  (component/system-map
   :loadimg (stego/loadimginit config)
   :showimg (component/using
             (stego/showimginit)
             [:loadimg])))

(set-init (fn [_] (example-system



                   replable-configs)))



















