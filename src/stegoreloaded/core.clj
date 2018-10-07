(ns stegoreloaded.core
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [com.stuartsierra.component :as component]
   [mikera.image.core :as imgcore]
   [mikera.image.colours :as imgrgb]
   [buddy.core.crypto :as crypto]
   [buddy.core.nonce :as nonce]
   [clojure.java.io :as io]
   [buddy.core.codecs :as codecs]
   [buddy.core.hash :as hash])
  (:gen-class))

(def iv  (codecs/to-bytes "ivisnotnecessary"))

(defn- decryptalgo [encryptedpart password]
  (def saltedpass (hash/sha256 password))

  (def convertpixels (->> encryptedpart
                          (map -)
                          (map char)
                          (apply str)))
  (-> (crypto/decrypt (codecs/hex->bytes convertpixels) saltedpass iv {:algorithm :aes128-cbc-hmac-sha256})
      (codecs/bytes->str)))

(defn decryptimg [componentState]
  (let [{:keys [startingimg password]} componentState
        buddyConfig (crypto/block-cipher :twofish :cbc)
        rawimage (imgcore/load-image startingimg)
        pixels (imgcore/get-pixels rawimage)
        encryptedpart (first (split-with #(not= % -9588211) pixels))]
    (println (decryptalgo encryptedpart password))))

(defn encryptimg [componentState]
  (let [{:keys [startingimg message password]} componentState
        buddyConfig (crypto/block-cipher :twofish :cbc)]
    (def original-text
      (codecs/to-bytes message))

    (def saltedpass (hash/sha256 password))
    (def encryptedhex  (codecs/bytes->hex (crypto/encrypt original-text saltedpass iv
                                                          {:algorithm :aes128-cbc-hmac-sha256})))
    (def inlenin (imgcore/load-image startingimg))
    (def pixels (imgcore/get-pixels inlenin))
    (def encryptedintegers (map int encryptedhex))
    (def pixelstostore  (count encryptedintegers))
    (dotimes [i (inc pixelstostore)]
      (if (= i pixelstostore)
        (aset pixels i -9588211)
        (aset pixels i (- (nth encryptedintegers i)))))
    (imgcore/set-pixels inlenin pixels)
    inlenin))


;;entrypoint component & init fn:::


(defrecord LOADCOMP [config]
  component/Lifecycle
  (start [this]
    (if (:decrypt (:config this))
      (assoc this :processedimg (decryptimg (:config this)))
      (assoc this :processedimg (encryptimg (:config this)))))
  (stop [this]
    this))

(defn loadimginit [config]
  (map->LOADCOMP {:config config}))



;;show-image component & init fn


(defrecord SHOWCOMP [config]
  component/Lifecycle
  (start [this]
;;rewrite path as comp dependency todo
    (when-not (:decrypt (:config this))
      (imgcore/save (:processedimg (:loadimg this)) (str (:startingimg (:config this)) ".enc.png")))
    (assoc this :finished true))
  (stop [this]
    this))

(defn showimginit [config]
  (map->SHOWCOMP {:config config}))




;;production configs


(defn prod-system [config]
  (component/system-map
   :loadimg (loadimginit config)
   :showimg (component/using
             (showimginit config)
             [:loadimg])))

(def cli-options
  [["-i" "--image Image" "Input image path"]
   ["-d" "--decrypt" "Decrypt mode"]
   ["-m" "--message Message" "Message to encrypt"
    :default "hello"]
   ["-p" "--password Password" "Password used for encryption"
    :default  "nopassword"]
   ["-h" "--help"]])

(defn -main [& args]
  (let [{:keys [image decrypt message password]}
        (:options (parse-opts args cli-options))]

    (if image
      (component/start (prod-system {:startingimg image
                                     :message message
                                     :password password
                                     :decrypt decrypt}))
      (println

       "You have to specify the host .png path with -i /path/to/img"))))


