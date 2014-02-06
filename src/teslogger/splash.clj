(ns teslogger.splash
  (:gen-class)
  (:use [seesaw core mig])
  (:require [teslogger.ui :as ui]))

(declare ^:dynamic splash-window)
(def product-title
  (label :icon "logo.png"))

(defn open-main-window [browser-spec _]
  (dispose! splash-window)
  (try
    (ui/make-main-window browser-spec)
    (catch Exception ex
      (alert (.toString ex)))))

(defn make-splash-window []
  (doto (window
         :content (mig-panel :items [[product-title "span,grow"]
                                 [(button :icon "ie.png"
                                          :listen [:action (partial open-main-window :ie)]) ""]
                                 [(button :icon "firefox.png"
                                          :listen [:action (partial open-main-window :firefox)]) ""]
                                 [(button :icon "chrome.png"
                                          :listen [:action (partial open-main-window :chrome)]) ""]]))
      pack!
      (.setLocationRelativeTo nil)
      (.setAlwaysOnTop true)
      show!))

(defn -main [& args]
  (def splash-window (make-splash-window)))

