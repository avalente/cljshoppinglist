(ns shoppinglist.controller
  (:use [clojure.tools.logging :only (info)]
        [slingshot.slingshot :only [try+]])
  (:require
    [shoppinglist.model :as model]
    [shoppinglist.utils :as utils]
    [cheshire.core :as json]))

(defn shopping-lists [req]
  (let [data (model/all-lists)]
    (utils/response data)))

(defn shopping-list [req]
  (let [{{user :user} :params} req
        data (model/read-shoppinglist user)]
    (if (nil? data)
      (utils/response-not-found "list not found for user: " user)
      (utils/response data))))

(defn shopping-list-update [req]
  (let [{{user :user} :params body :body headers :headers} req
        ctype (headers "content-type")]
    (if (nil? (re-find #"^application/json" ctype))
      (utils/bad-request (str "bad content type:" ctype))
      (let [sl (utils/parse-shopping-list (slurp body))]
        (if (nil? sl)
          (utils/bad-request (str "invalid request body"))
          (do
            (try+
              (utils/response (model/save-shoppinglist user sl))
              (catch Object e
                (do
                  (info "Can't save the object: " e)
                  (utils/response "can't save the object" :status 500))))))))))

