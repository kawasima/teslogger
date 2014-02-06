(ns teslogger.ui
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:use [teslogger.core]
        [seesaw core mig chooser color border]
        [clj-webdriver.taxi :only [new-driver set-driver! quit]]))

(declare clear-button)

(def parent-frame
  (frame
   :title "teslogger"
   :on-close :dispose
   :listen [:window-closed
            (fn [e] (quit))]))

(def run-progress-bar
  (progress-bar :paint-string? true))

(def run-message-label
   (text :multi-line? true
         :wrap-lines? true
         :editable? false
         :visible? false))

(def run-message-panel
  (scrollable run-message-label :visible? false))

(def case-id-field
  (text :id :case-id
        :text ""
        :border nil))

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
      (when (string/blank? (text case-id-field))
        (let [fname (.getName file)]
          (text! case-id-field (.substring fname 0 (.lastIndexOf fname ".")))))
      (.setForeground run-progress-bar (color :limegreen))
      (show! run-progress-bar)
      (pack! parent-frame)
      (run-script! file
                   (text case-id-field)
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
      (finally (. parent-frame setEnabled true)
               (show! clear-button)))))

(defn clear-result! []
  (hide! run-progress-bar)
  (hide! clear-button)
  (hide! run-message-panel))

(def clear-button
  (button :text "clear"
          :background (color "#000000" 0)
          :border nil
          :action (action :handler (fn [e]
                                     (text! case-id-field "")
                                     (clear-result!))
             :icon "close.png")))

(def load-script-action
  (action
   :handler (fn [e] (choose-file :type :open
                                 :dir (io/file ".")
                                 :success-fn (fn [fc file]
                                               (.start
                                                (Thread. (run-script-fn file))))))
   :name "load"))

(defn load-script-button []
  (let [btn (button :text "load..."
                    :background "#e8970f"
                    :foreground "#ecf0f1")]
    (set-action* btn load-script-action)
    btn))

(defn take-ss-button []
  (let [btn (button :text "Take!" :background "#d8d0bb")]
    (set-action* btn take-screenshot-action)
    btn))

(defn make-main-window [browser-spec]
  (let [driver (new-driver {:browser browser-spec})]
    (config!
     parent-frame
     :content (mig-panel
               :constraints ["" (apply str (repeat 12 "[16]")) ""]
               :items [[(label :text "Test case ID" :foreground "#ecf0f1"), "span 3,r"]
                       [case-id-field "l,span 6,grow"]
                       [(load-script-button) "span 3,wrap"]
                       [run-message-label "span,grow,hidemode 1"]
                       [run-progress-bar "span 11, grow, hidemode 1"]
                       [clear-button   "span 1, wrap"]
                       [(take-ss-button) "span 12,growx,wrap"]
                       [(button :text "OK"
                                :background "#1abc9c"
                                :foreground "#ecf0f1"
                                :border (empty-border :thickness 10)), "span 6,wmin 100,center"]
                       [(button :text "NG"
                                :foreground "#ecf0f1"
                                :background "#d65430"
                                :border (empty-border :thickness 10)), "span 6,wmin 100, center"]]
               :background "#2c3e50"))
    (clear-result!)
    (doto parent-frame
      ;;(.setUndecorated true)
      (pack!)
      (show!)
      (.setAlwaysOnTop true))
    (set-driver! driver)))
