(ns shoppinglist.core
  (:use [compojure.core :only (defroutes GET PUT ANY)]
        [ring.adapter.jetty :as ring])
  (:require 
    [shoppinglist.controller :as c]
    [shoppinglist.model :as model]))

(defroutes routes
  (GET "/lists" [] (c/shopping-lists))
  (GET "/lists/:user" [user] (c/shopping-list user))
  (PUT "/lists/:user" {{user :user} :params body :body headers :headers}
       (c/shopping-list-update user (slurp body) headers))
  (ANY "*" {uri :uri} (c/not-found (str "resource not found: " uri))))

(defn init []
  (model/initialize (System/getenv "DSN")))

(defn -main []
  (init)
  (run-jetty routes {:port 8080 :join? false}))
