(ns shoppinglist.controller
  (:use [clojure.tools.logging :only (info)]
        [slingshot.slingshot :only [try+]])
  (:require
    [shoppinglist.model :as model]
    [cheshire.core :as json]))

(defn not-found [& [message]]
  (let [msg (if (nil? message) "resource not found" message)
        data (json/generate-string msg)]
    {:status 404, :content-type "application/json", :body data}))

(defn bad-request [& [message]]
  (let [msg (if (nil? message) "bad request" message)
        data (json/generate-string msg)]
    {:status 400, :content-type "application/json", :body data}))

(defn shopping-lists []
  (let [data (json/generate-string (model/all-lists))]
    {:status 200, :content-type "application/json", :body data}))

(defn shopping-list [user]
  (let [raw (model/read-shoppinglist user)
        data (json/generate-string raw)]
    (if (nil? raw)
      (not-found)
      {:status 200, :content-type "application/json", :body data})))

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

(defn shopping-list-update [user body headers]
  (let [ctype (headers "content-type")]
    (if (nil? (re-find #"^application/json" ctype))
      (bad-request (str "bad content type:" ctype))
      (let [sl (parse-shopping-list body)]
        (if (nil? sl)
          (bad-request (str "invalid request body"))
          (do
            (try+
              {:status 200, :content-type "application/json",
               :body (json/generate-string (model/save-shoppinglist user sl))}
              (catch Object e
                (do
                  (info "Can't save the object: " e)
                  {:status 500, :content-type "application/json", :body "can't save the object"})))))))))
