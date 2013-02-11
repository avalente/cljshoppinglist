(ns shoppinglist.auth
  (:require
    [cheshire.core :as json]
    [shoppinglist.controller :as c]
    [shoppinglist.model :as model]
    [shoppinglist.utils :as utils]))

(def users {"user" {:password "user_pwd"
                    :roles ["user"]}
            "root" {:password "root_pwd"
                    :roles ["admin"]}})

(def roles {"user" ["list-view", "list-edit", "list-delete"],
            "admin" ["list-view-all", "list-view", "list-edit", "list-delete"]})

(defn get-user [username]
  (assoc (get users username) :username username))

(defn get-request-user [request]
  (let [session (:session request)]
    (if (or (nil? session) (empty? session))
      nil
      (get-user (:username session)))))

(defn get-user-permissions [user]
  (set (flatten (for [role (:roles user)] (get roles role)))))

(defn has-permission? [request permission]
  (contains? (get-user-permissions (get-request-user request)) permission))

(defn is-owner? [request]
  (let [{{owner :user} :params} request
        username (get (get-request-user request) :username)]
    (and (not (nil? username)) (not (nil? owner)) (= username owner))))

(defn authenticate [username password]
  (let [user-data (get-user username)]
    (if (nil? user-data)
      nil
      (if (= password (:password user-data))
        user-data
        nil))))

(defn authorized? [request predicates target]
  (let [res (for [pred predicates
              :let [fun (first pred)
                    args (rest pred)]]
              (apply fun (cons request args)))]
    (if (every? identity res)
      (target request)
      (utils/forbidden request))))

(defn login [ {{username "username" password "password"} :form-params} ]
  (let [auth (authenticate username password)]
    (if (nil? auth)
      (utils/unauthorized [])
      {:status 200 :session (assoc auth :username username)})))

(defn whoami [{session :session}]
  (utils/response (:username session)))

;; TODO: add more authorization factors
(defn check-cookie [session]
  (if (or (nil? session) (empty? session))
    false
    true))

(defn wrap-auth
  "Middleware to check for authentication/authorization"
  [handler auth-function excluded]
  (fn [request]
    (if (contains? (set excluded) (:uri request))
      (handler request)
      (if (check-cookie (:session request))
        (handler request)
        (utils/unauthorized request)))))

