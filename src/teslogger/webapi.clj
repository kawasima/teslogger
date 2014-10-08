(ns teslogger.webapi
  (:require [liberator.dev :as dev]
            [teslogger.ui :as ui])
  (:use [teslogger.netty :only [run-netty]]
        [liberator.core :only [defresource]]
        [seesaw.core :only [text]]
        [compojure.core :only [defroutes ANY routes]]))

(defresource current-case
  :available-media-types ["application/json"]
  :handle-ok (fn [ctx]
               {:id (text ui/case-id-field)}))

(defn app []
  (->
   (routes
    (ANY "/current-case" [] current-case)
    (dev/wrap-trace :ui :header))))
