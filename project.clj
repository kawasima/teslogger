(defproject net.unit8.teslogger/teslogger "0.2.0-SNAPSHOT"
  :description "A tool for taking screenshots."
  :url "https://github.com/kawasima/teslogger"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-webdriver "0.6.1"]
                 [compojure "1.2.0"]
                 [liberator "0.12.2"]
                 [seesaw "1.4.4"]
                 [clj-time "0.8.0"]
                 [environ "1.0.0"]
                 [net.unit8/ulon-colon "0.2.0"]
                 [overtone/at-at "1.2.0"]]
;  :aot []
  :main teslogger.splash)

