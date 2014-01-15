(ns teslogger.ui
  (:require [clojure.java.io :as io])
  (:use [teslogger.core]
        [seesaw core mig chooser color]
        [clj-webdriver.taxi :only [new-driver set-driver! quit]]))

(def parent-frame
  (frame
   :title "Capturable Web Browser"
   :on-close :dispose
   :listen [:window-closed
            (fn [e] (quit))]))

(def run-progress-bar
  (progress-bar :paint-string? true))

(def run-message-label
   (text :multi-line? true
           :wrap-lines? true
           :editable? false))

(def run-message-panel
  (scrollable run-message-label :visible? false))

(def case-id-field
  (text :id :case-id :text ""))

(def take-screenshot-action
  (action
   :handler (fn [e]
              (let [cid (text case-id-field)]
                (take-screenshot-handler cid)))
   :icon "camera.png"))


(defn run-script-fn [file]
  (fn []
    (try
      (. parent-frame setEnabled false)
      (.setForeground run-progress-bar (color :limegreen))
      (show! run-progress-bar)
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
        (show! run-message-panel)
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
  (let [btn (button :text "load...")]
    (set-action* btn load-script-action)
    btn))

(defn take-ss-button []
  (let [btn (button :text "Take!")]
    (set-action* btn take-screenshot-action)
    btn))

(defn make-main-window [browser-spec]
  (let [driver (new-driver {:browser browser-spec})]
    (config!
     parent-frame
     :content (mig-panel
               :items [["Test case ID", "l"]
                       [case-id-field "l,width 100!"]
                       [(load-script-button) "wrap"]
                       [run-message-label "span,grow,hidemode 1"]
                       [run-progress-bar "span,grow, hidemode 1"]
                       [(take-ss-button) "span,grow"]
                       [(button :text "OK"), "l"]
                       [(button :text "NG")]]))
    (.hide run-progress-bar)
    (-> parent-frame pack! show! (.setAlwaysOnTop true))
    (set-driver! driver)))

