(ns shoppinglist.utils
  (:use [clojure.tools.logging :only (info)]
        [slingshot.slingshot :only [try+]])
  (:require
    [shoppinglist.model :as model]
    [cheshire.core :as json]))

(defn response [body & {:keys [status content-type headers]
                        :or {status 200 content-type "application/json;charset=UTF-8" headers {}}}]
  {:status status
   :headers (assoc headers "content-type" content-type)
   :body (json/generate-string body)})

(defn response-not-found [& [message]]
  (let [msg (if (nil? message) "resource not found" message)
        data (json/generate-string msg)]
    (response data :status 404)))

(defn bad-request [& [message]]
  (let [msg (if (nil? message) "bad request" message)
        data (json/generate-string msg)]
    (response data :status 400)))

(defn unauthorized [req]
  (response "Unauthorized" :status 401))

(defn forbidden [req]
  (response "Forbidden" :status 403))

;; Controllers helper functions

(defn build-shopping-list [data]
  (if (not (vector? data))
    (do
      (info "Can't build shopping list: not a list: " data)
      nil)
    (doall
      (for [{item "item" qty "quantity" unit "unit" pri "priority"} data]
        (model/new-shoppinglist-item item qty unit pri)))))

(defn parse-shopping-list [data]
  (try+
    (let [parsed (json/parse-string data)]
      (build-shopping-list parsed))
    (catch [:type :shoppinglist.model/invalid-item] {msg :message value :value}
      (do
        (info "Can't decode json: " msg value)
        nil))
    (catch Object msg
      (do
        (info "Can't decode json: " msg)
        nil))))

