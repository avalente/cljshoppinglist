(ns shoppinglist.model
  (:require
    [clojure.java.jdbc :as sql]
    [clojure.tools.logging :as log]
    [cheshire.core :as json]
    [shoppinglist.utils :as u])
  (:use 
    [clojure.tools.logging :only (info)]
    [slingshot.slingshot :only [try+ throw+]]))

(def dsn)

(defn initialize [new-dsn]
  (log/info "Initializing model on " new-dsn)
  (def dsn new-dsn))

(defn new-shoppinglist-item [item & [quantity unit priority]]
  (if (not (string? item))
    (throw+ {:type ::invalid-item :message "bad item" :value item})
    (if (not (or (nil? unit) (string? unit)))
      (throw+ {:type ::invalid-item :message "bad unit" :value unit})
      (if (not (or (nil? quantity) (number? quantity)))
        (throw+ {:type ::invalid-item :message "bad quantity" :value quantity})
        (if (not (or (nil? priority) (integer? priority)))
          (throw+ {:type ::invalid-item :message "bad priority" :value priority})
          {:item item, :quantity quantity, :unit unit, :priority priority})))))

(defn new-shoppinglist [user & [data]]
  {:user user,
   :list (map (fn [x] (apply new-shoppinglist-item x)) data)
   :created nil,
   :updated nil})

(defn- get-shoppinglist [user]
  (sql/with-query-results 
    res
    ["SELECT * FROM shoppinglist.lists WHERE \"user\"=?" user]
    (if (empty? res)
      nil
      (let [raw (first res)]
        {:user user
         :created (:created raw)
         :updated (:updated raw)
         :list (json/parse-string (:data raw))}))))

(defn read-shoppinglist [user]
  (sql/with-connection
    dsn
    (get-shoppinglist user)))

(defn save-shoppinglist [user list]
  (log/debug "Saving list for user " user ": " list)
  (let [json (json/generate-string list)]
    (sql/with-connection 
      dsn
      (sql/transaction
        (sql/update-or-insert-values
          :shoppinglist.lists
          ["\"user\"=?" user]
          {(keyword "\"user\"") user,
           :data json})
        (get-shoppinglist user)))))

(defn all-lists []
  (sql/with-connection
    dsn
    (sql/with-query-results
      res
      ["SELECT \"user\", created, updated FROM shoppinglist.lists ORDER BY \"user\""]
      (into [] res))))

(defn create-user [email real-name password ip]
  (let [data {:email email :real_name real-name :ip ip
              :password (u/hash-password password)
              :request_id (u/generate-uuid)}]
    (sql/with-connection
      dsn
      (sql/transaction
        (sql/insert-records 
          :shoppinglist.users
          data)))))

(defn check-registration-request [id]
  (sql/with-connection
    dsn
    (sql/with-query-results 
      res
      ["SELECT * FROM shoppinglist.users WHERE request_id=?" (u/to-uuid id)]
      (if (empty? res)
        false
        (let [rec (first res)]
          ;TODO: controllare che la richiesta non sia troppo vecchia
          (not (:enabled rec)))))))

(defn accept-user [id]
  (sql/with-connection
    dsn
    (sql/update-values
      :shoppinglist.users
      ["request_id=?" (u/to-uuid id)]
      {:enabled true :request_id nil})))

(defn get-user [email]
  (sql/with-connection
    dsn
    (sql/with-query-results 
      res
      ["SELECT * FROM shoppinglist.users WHERE email=?" email]
      (if (empty? res)
        nil
        (let [rec (first res)]
          (if (:enabled rec)
            {:roles (vec (.getArray (:roles rec)))
             :created (:created rec)
             :password (:password rec)
             :real-name (:real_name rec)
             :username (:email rec)}
            (do
              (log/info "User " email "is disabled")
              nil)))))))

;; Controllers helper functions

(defn build-shopping-list [data]
  (if (not (vector? data))
    (do
      (info "Can't build shopping list: not a list: " data)
      nil)
    (doall
      (for [{item "item" qty "quantity" unit "unit" pri "priority"} data]
        (new-shoppinglist-item item qty unit pri)))))

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
