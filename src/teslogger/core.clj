(ns teslogger.core
  (:gen-class)
  (:require
    [seesaw.core :as s]
    [clojure.java.io :as io])
  (:use
    [clj-webdriver.taxi]
    [clj-time.core]
    [clj-time.format]))

(defn app []
  (set-driver! {:browser :firefox} "https://github.com")
  (click "a[href*='login']")
  (wait-until #(.contains (title) "Sign in"))
  (input-text "#login_field" "your-username")
  (input-text "#password" "your-password")
  (submit "#password")
  (println (page-source))
  (take-screenshot :file "hoge.png")
  (quit))

(defn make-widget [driver]
  (let [ take-ss (s/button :text "capture!"
                   :icon (s/icon (io/file "resources/camera.png")))
         f (s/frame :title "Capturable Web Browser"
             :on-close :exit)
         display (fn [content]
                   (s/config! f :content content)
                   content)]
    (s/listen take-ss
      :action (fn [e] (take-screenshot
                        driver
                        :file
                        (str
                          (unparse (formatter "yyyyMMddhhmmss") (now)) ".png"))))
    (display take-ss)
    (-> f s/pack! s/show! (.setAlwaysOnTop true))))

(defn -main [& args]
  (let [driver (new-driver {:browser :firefox})]
    (make-widget driver)
    (set-driver! driver)))
