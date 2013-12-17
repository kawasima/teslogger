(ns teslogger.core
  (:gen-class)
  (:require
    [seesaw.core :as s]
    [clojure.java.io :as io])
  (:use
    [clj-webdriver.taxi]
    [seesaw.mig]
    [clj-time.core]
    [clj-time.format]))


(defn make-widget [driver]
  (let [ take-ss (s/button
                   :icon (s/icon "camera.png"))
         case-id (s/text :id :case-id :text "")
         f (s/frame
            :title "Capturable Web Browser"
            :on-close :dispose
            :listen [:window-closed
                     (fn [e] (quit driver))])]

    (s/config!
     f
     :content (mig-panel
               :items [["Test case ID", "l"]
                       [case-id, "l,width 100!,wrap"]
                       [take-ss, "span 2,wrap"]
                       [(s/button :text "OK"), "l"]
                       [(s/button :text "NG")]]))
    (s/listen take-ss
              :action (fn [e]
                        (let [cid (s/text case-id)
                              ss-path (io/file "screenshots"
                                               (if (empty? cid) "other" cid)
                                               (str (unparse (formatter "yyyyMMddhhmmss") (now))
                                                    ".png"))]
                          (io/make-parents ss-path)
                          (take-screenshot driver :file ss-path))))
    (-> f s/pack! s/show! (.setAlwaysOnTop true))))

(defn -main [& args]
  (let [driver (new-driver {:browser :ie})]
    (make-widget driver)
    (set-driver! driver)))
