(ns shoppinglist.utils
  (:use [clojure.tools.logging :only (info)]
        [slingshot.slingshot :only [try+]])
  (:require
    [cheshire.core :as json])
  (:import
    [java.util UUID]
    [org.mindrot.jbcrypt BCrypt]))

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


(defn hash-password
  "Hashes a given plaintext password using bcrypt and an optional
   :work-factor (defaults to 10)"
  [password & {:keys [work-factor]}]
  (BCrypt/hashpw password (if work-factor
                            (BCrypt/gensalt work-factor)
                            (BCrypt/gensalt))))

(defn check-password
  "Returns true if the plaintext [password] corresponds to [hash]"
  [password hash]
  (BCrypt/checkpw password hash))

(defn generate-uuid
  "Returns a random UUID"
  []
  (UUID/randomUUID))

(defn to-uuid [uuid]
  (UUID/fromString uuid))
