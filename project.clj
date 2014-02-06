(defproject teslogger "0.1.0"
  :description "A tool for taking screenshots."
  :url "https://github.com/kawasima/teslogger"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [ [org.clojure/clojure "1.5.1"]
                  [clj-webdriver "0.6.0"]
                  [seesaw "1.4.4"]
                  [clj-time "0.6.0"]]
  :aot :all
  :main teslogger.splash)


