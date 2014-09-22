(ns teslogger.core
  (:require [clojure.java.io :as io]
            [clj-time.core :as tm]
            [clj-time.format :as tm-fmt])
  (:use [clj-webdriver.taxi :rename {take-screenshot taxi-take-screenshot}]
        [ulon-colon.producer :only [start-producer produce]]))

(def ^:dynamic *cid*)

(defn take-screenshot-handler [cid]
  (let [ss-path (io/file "screenshots"
                         (if (empty? cid) "other" cid)
                         (str (tm-fmt/unparse (tm-fmt/formatter "yyyyMMddhhmmss") (tm/now))
                              ".png"))]
    (io/make-parents ss-path)
    (taxi-take-screenshot :file ss-path)))

(defn take-screenshot
  ([]
   (take-screenshot-handler *cid*))
  ([& opts] (apply taxi-take-screenshot opts)))

(defn read-scripts [file]
  (with-open [r  (java.io.PushbackReader. (io/reader file))]
    (loop [s []]
      (if-let [v (read r false nil)]
        (recur (conj s v))
        s))))

(defn run-script! [file cid & {callback-fn :callback
                               setup-fn    :setup
                               teardown-fn :teardown}]
  (let [nspace (create-ns (gensym "sandbox"))
        exp-seq (read-scripts (.getAbsolutePath file))]
    (binding [*ns*  nspace
              *cid* cid]
      (refer-clojure)
      (use '[clj-webdriver.taxi :exclude [take-screenshot]])
      (refer 'teslogger.core :only '[take-screenshot])
      (when setup-fn (setup-fn (count exp-seq)))
      (loop [exp exp-seq idx 1]
        (when (seq exp)
          (do (when callback-fn (callback-fn idx))
            (eval (first exp)))
          (recur (rest exp) (inc idx))))
      (when teardown-fn (teardown-fn (count exp-seq))))))


