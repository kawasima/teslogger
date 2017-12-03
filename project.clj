(defproject net.unit8.teslogger/teslogger "0.3.0"
  :description "A tool for taking screenshots."
  :url "https://github.com/kawasima/teslogger"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.seleniumhq.selenium/selenium-java "3.8.0"]
                 [org.seleniumhq.selenium/selenium-remote-driver "3.8.0"]
                 [org.seleniumhq.selenium/selenium-server "3.8.0"]
                 [clj-webdriver "0.7.2"]
                 [compojure "1.6.0"]
                 [liberator "0.15.1"]
                 [seesaw "1.4.5"]
                 [clj-time "0.14.2"]
                 [environ "1.1.0"]
                 [net.unit8/ulon-colon "0.2.2"]
                 [overtone/at-at "1.2.0"]]
  :aot [teslogger.splash]
  :main teslogger.splash)
