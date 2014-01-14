(ns teslogger.core
  (:gen-class)
  (:require [seesaw.core :as s]
            [clojure.java.io :as io]
            [clj-time.core :as tm]
            [clj-time.format :as tm-fmt])
  (:use [clj-webdriver.taxi :rename {take-screenshot taxi-take-screenshot}]
        [seesaw mig chooser action color]))

(def parent-frame
  (s/frame
   :title "Capturable Web Browser"
   :on-close :dispose
   :listen [:window-closed
            (fn [e] (quit))]))

(def run-progress-bar
  (s/progress-bar :paint-string? true))

(def run-message-label
   (s/text :multi-line? true
           :wrap-lines? true
           :editable? false))

(def run-message-panel
  (s/scrollable run-message-label :visible? false))

(def case-id-field
  (s/text :id :case-id :text ""))

(def take-screenshot-action
  (action
   :handler (fn [e]
              (let [cid (s/text case-id-field)
                    ss-path (io/file "screenshots"
                                     (if (empty? cid) "other" cid)
                                     (str (tm-fmt/unparse (tm-fmt/formatter "yyyyMMddhhmmss") (tm/now))
                                          ".png"))]
                (io/make-parents ss-path)
                (taxi-take-screenshot :file ss-path)))
   :icon "camera.png"))

(defn take-screenshot
  ([]
   (.actionPerformed take-screenshot-action nil))
  ([& opts] (apply taxi-take-screenshot opts)))

(defn read-scripts [file]
  (with-open [r  (java.io.PushbackReader. (io/reader file))]
    (loop [s []]
      (if-let [v (read r false nil)]
        (recur (conj s v))
        s))))

(defn run-script! [file & {callback-fn :callback
                           setup-fn    :setup
                           teardown-fn :teardown}]
  (let [nspace (create-ns (gensym "sandbox"))
        exp-seq (read-scripts (.getAbsolutePath file))]
    (binding [*ns* nspace]
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

(defn run-script-fn [file]
  (fn []
    (try
      (. parent-frame setEnabled false)
      (.setForeground run-progress-bar (color :limegreen))
      (s/show! run-progress-bar)
      (run-script! file
                   :setup #(doto run-progress-bar
                             (.setMaximum %)
                             (.setMinimum 0)
                             (.setValue 0))
                   :callback #(doto run-progress-bar
                                (.setValue %))
                   :teardown #(doto run-progress-bar
                                (.setValue %)))
      (catch AssertionError ex
        (. run-message-label setText (.getMessage ex))
        (s/show! run-message-panel)
        (.setForeground run-progress-bar (color :crimson)))
      (finally (. parent-frame setEnabled true)))))

(def load-script-action
  (action
   :handler (fn [e] (choose-file :type :open
                                 :dir (io/file ".")
                                 :success-fn (fn [fc file]
                                               (.start
                                                (Thread. (run-script-fn file))))))
   :name "load"))

(defn load-script-button []
  (let [btn (s/button :text "load...")]
    (s/set-action* btn load-script-action)
    btn))

(defn take-ss-button []
  (let [btn (s/button :text "Take!")]
    (s/set-action* btn take-screenshot-action)
    btn))

(defn make-widget [driver]
  (s/config!
   parent-frame
   :content (mig-panel
             :items [["Test case ID", "l"]
                     [case-id-field "l,width 100!"]
                     [(load-script-button) "wrap"]
                     [run-message-label "span,grow,hidemode 1"]
                     [run-progress-bar "span,grow, hidemode 1"]
                     [(take-ss-button) "span,grow"]
                     [(s/button :text "OK"), "l"]
                     [(s/button :text "NG")]]))
  (.hide run-progress-bar)
  (-> parent-frame s/pack! s/show! (.setAlwaysOnTop true)))

(defn -main [& args]
  (let [driver (new-driver {:browser :ie})]
    (make-widget driver)
    (set-driver! driver)))
