(defproject net.unit8.teslogger/teslogger "0.2.2"
  :description "A tool for taking screenshots."
  :url "https://github.com/kawasima/teslogger"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.seleniumhq.selenium/selenium-java "2.44.0"]
                 [org.seleniumhq.selenium/selenium-remote-driver "2.44.0"]
                 [org.seleniumhq.selenium/selenium-server "2.44.0"]
                 [clj-webdriver "0.6.1"]
                 [compojure "1.3.1"]
                 [liberator "0.12.2"]
                 [seesaw "1.4.5"]
                 [clj-time "0.9.0"]
                 [environ "1.0.0"]
                 [net.unit8/ulon-colon "0.2.2"]
                 [overtone/at-at "1.2.0"]]
  :aot [teslogger.splash]
  :main teslogger.splash)

