(ns teslogger.sender
  (:use [ulon-colon.producer :only [start-producer produce stop-producer]]
        [clojure.core.async :only [<!!]]
        [environ.core]
        [overtone.at-at])
  (:import [java.io ByteArrayOutputStream DataOutputStream File]
           [java.net InetSocketAddress InetAddress]
           [java.nio ByteBuffer]
           [java.nio.file Files]
           [java.nio.channels DatagramChannel])
  (:refer-clojure :exclude [send]))

(def producer (atom nil))
(def pool (mk-pool))
(def job  (atom nil))

(defn notify []
  (let [baos (ByteArrayOutputStream.)
        dos  (DataOutputStream. baos)
        ch   (DatagramChannel/open)]
    (try 
      (doto dos
        (.write (.getAddress (InetAddress/getLocalHost)) 0 4)
        (.writeInt (or (env :teslogger-sender-port) 5629)))
      (.. ch socket (setBroadcast true))
      (.send ch
        (ByteBuffer/wrap (.toByteArray baos))
        (InetSocketAddress. "255.255.255.255" (or (env :teslogger-server-port) 56294)))
      (finally (.close ch)))))

(defn send! []
  (let [targets (->> (file-seq (File. "screenshots"))
                     (filter #(.isFile %)))]
    (doseq [target targets]
      (when (= :commit (<!!(produce {:name (.. target getName)
                              :case-id (.. target getParentFile getName)
                              :body (Files/readAllBytes (.toPath target))}))) 
        (.delete target)))))

(defn start-sender []
  (notify)
  (reset! producer (start-producer))
  (reset! job (every 5000 #(send!) pool)))

(defn stop-sender []
  (stop-producer @producer)
  (kill @job)
  (.shutdown (:thread-pool @(:pool-atom pool)))
  (reset! producer nil))
