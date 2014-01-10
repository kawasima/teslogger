(ns teslogger.core
  (:gen-class)
  (:require [seesaw.core :as s]
            [clojure.java.io :as io]
            [clj-time.core :as tm]
            [clj-time.format :as tm-fmt])
  (:use [clj-webdriver.taxi :rename {take-screenshot taxi-take-screenshot}]
        [seesaw mig chooser action]))

(def parent-frame
  (s/frame
   :title "Capturable Web Browser"
   :on-close :dispose
   :listen [:window-closed
            (fn [e] (quit))]))

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
                (taxi-take-screenshot :file ss-path)))))

(defn take-screenshot
  ([]
   (.actionPerformed take-screenshot-action nil))
  ([& opts] (apply taxi-take-screenshot opts)))

(defn run-script! [file]
  (let [nspace (create-ns (gensym "sandbox"))]
    (binding [*ns* nspace]
      (refer-clojure)
      (use '[clj-webdriver.taxi :exclude [take-screenshot]])
      (refer 'teslogger.core :only '[take-screenshot])
      (load-file (.getAbsolutePath file)))))

(def load-script-action
  (action
   :handler (fn [e] (choose-file :type :open
                                 :dir (io/file ".")
                                 :success-fn (fn [fc file]
                                               (. parent-frame setEnabled false)
                                               (try
                                                 (run-script! file)
                                                 (finally (. parent-frame setEnabled true)))
                                               )))
   :name "load"))

(defn load-script-button []
  (let [btn (s/button :text "load...")]
    (s/set-action* btn load-script-action)
    btn))

(defn make-widget [driver]
  (let [ take-ss (s/button
                   :icon (s/icon "./resources/camera.png")
                  :text "Screenshot")
         ]

    (s/config!
     parent-frame
     :content (mig-panel
               :items [["Test case ID", "l"]
                       [case-id-field "l,width 100!"]
                       [(load-script-button) "wrap"]
                       [take-ss "span,grow,height 100"]
                       [(s/button :text "OK"), "l"]
                       [(s/button :text "NG")]]))
    (s/set-action* take-ss take-screenshot-action)
    (-> parent-frame s/pack! s/show! (.setAlwaysOnTop true))))

(defn -main [& args]
  (let [driver (new-driver {:browser :ie})]
    (make-widget driver)
    (set-driver! driver)))
