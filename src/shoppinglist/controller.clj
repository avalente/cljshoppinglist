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
      (let [sl (model/parse-shopping-list (slurp body))]
        (if (nil? sl)
          (utils/bad-request (str "invalid request body"))
          (do
            (try+
              (utils/response (model/save-shoppinglist user sl))
              (catch Object e
                (do
                  (info "Can't save the object: " e)
                  (utils/response "can't save the object" :status 500))))))))))

;TODO: controllo errori
(defn register [req]
  (let [{body :body ip :remote-addr} req
        data (json/parse-string (slurp body))
        email (get data "email")
        real-name (get data "real_name")
        password (get data "password")
        res (first (model/create-user email real-name password ip))]
    ;TODO: mandare email
    (println (:request_id res))
    (utils/response
      (str "Check your email, " (:real_name res)))))

;TODO: controllo errori
(defn accept [req]
  (let [{{id :id} :params} req]
    (println req id)
    (if (model/check-registration-request id)
      (model/accept-user id)
      (utils/response "bad request" :status 400))))
