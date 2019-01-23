(ns stegoreloaded.core
  (:require
   [buddy.core.crypto :as crypto]
   [buddy.core.codecs :as codecs]
   [buddy.core.hash :as hash]
   [clojure.tools.cli :refer [parse-opts]]
   [com.stuartsierra.component :as component]
   [mikera.image.core :as imgcore])
  (:gen-class))

(def iv (codecs/to-bytes "ivisnotnecessary"))

(defn- decryptalgo [encryptedpart password]
  (let [saltedpass (hash/sha256 password)
        converted-pixels (->> encryptedpart
                              (map -)
                              (map char)
                              (apply str))]
    (-> (crypto/decrypt
         (codecs/hex->bytes converted-pixels)
         saltedpass
         iv
         {:algorithm :aes128-cbc-hmac-sha256})
        (codecs/bytes->str))))

(defn decryptimg [componentState]
  (let [{:keys [startingimg password]} componentState
        buddy-config (crypto/block-cipher :twofish :cbc)
        rawimage (imgcore/load-image startingimg)
        pixels (imgcore/get-pixels rawimage)
        encrypted-part (first (split-with #(not= % -9588211) pixels))]
    (println (decryptalgo encrypted-part password))))

(defn encryptimg [componentState]
  (let [{:keys [startingimg message password]} componentState
        buddy-config (crypto/block-cipher :twofish :cbc)
        original-text (codecs/to-bytes message)
        salted-pass (hash/sha256 password)
        encrypted-hex (codecs/bytes->hex (crypto/encrypt
                                          original-text salted-pass iv
                                          {:algorithm :aes128-cbc-hmac-sha256}))
        inlenin (imgcore/load-image startingimg)
        pixels (imgcore/get-pixels inlenin)
        encrypted-integers (map int encrypted-hex)
        pixelstostore (count encrypted-integers)]
    (dotimes [i (inc pixelstostore)]
      (if (= i pixelstostore)
        (aset pixels i -9588211)
        (aset pixels i (- (nth encrypted-integers i)))))
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
      (imgcore/save
       (:processedimg (:loadimg this))
       (str (:startingimg (:config this)) ".enc.png")))
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
    (if (some? image)
      (component/start (prod-system {:startingimg image
                                     :message message
                                     :password password
                                     :decrypt decrypt}))
      (println "You have to specify the host .png path with -i /path/to/img"))))
