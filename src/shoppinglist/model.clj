(ns shoppinglist.model
  (:require
    [clojure.java.jdbc :as sql]
    [clojure.tools.logging :as log]
    [cheshire.core :as json])
  (:use [slingshot.slingshot :only [throw+]]))

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
