(ns com.stego.stego-reloaded
        (:require
	 [com.stuartsierra.component :as component]
	 [mikera.image.core :as imgcore]
         [mikera.image.colours :as imgrgb]
	 [buddy.core.crypto :as crypto]
	 [buddy.core.nonce :as nonce]
	 [buddy.core.codecs :as codecs]
	 [buddy.core.hash :as hash]
         )
)


(defn processimg [componentState]
(let [hostImage (:startingimg componentState)
      message (:message componentState)
      password (:password componentState)
      buddyConfig (crypto/block-cipher :twofish :cbc)
     ]

(def original-text
  (codecs/to-bytes message))
(def iv (codecs/to-bytes "ivisnotnecessary"))
(def saltedpass (hash/sha256 password))
(def encryptedhex  (codecs/bytes->hex (crypto/encrypt original-text saltedpass iv
                               {:algorithm :aes128-cbc-hmac-sha256})))
    (def inlenin (imgcore/load-image-resource hostImage))
    (def pixels (imgcore/get-pixels inlenin))
(def encryptedintegers (map #(read-string (apply str %)) (partition 3 (map #(str (int %)) encryptedhex))))
(def pixelstostore  (count encryptedintegers))
    (dotimes [i (inc pixelstostore)]
(if (= i pixelstostore)
(aset pixels i 00)
      (aset pixels i (nth encryptedintegers i)))
    )
    (imgcore/set-pixels inlenin pixels)
    inlenin)
)



;;entrypoint component & init fn:::
(defrecord LOADCOMP [config]
  component/Lifecycle
  (start [this]
 (assoc this :processedimg (processimg (:config this))))
  (stop [this]
    this))

(defn loadimginit [config]
 (map->LOADCOMP {:config config}))



;;show-image component & init fn:::
(defrecord SHOWCOMP []
  component/Lifecycle
  (start [this]
(imgcore/show (:processedimg (:loadimg this)))
 (assoc this :finished true))
  (stop [this]
    this))

(defn showimginit []
 (map->SHOWCOMP {:triggershow! true}))


;;todo decryption

(comment
(def decrypted (-> (crypto/decrypt (codecs/hex->bytes encrypted) saltedpass iv {:algorithm :aes128-cbc-hmac-sha256})
    (codecs/bytes->str)))
(println decrypted)
)




