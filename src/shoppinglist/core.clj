(ns shoppinglist.core
  (:use [compojure.core :only (defroutes GET POST PUT ANY)]
        [compojure.route :as route]
        [ring.adapter.jetty :as ring]
        [ring.middleware.cookies :only [wrap-cookies]]
        [ring.middleware.params :only [wrap-params]]
        [ring.middleware.nested-params :only [wrap-nested-params]]
        [ring.middleware.keyword-params :only [wrap-keyword-params]]
        [ring.middleware.session :only [wrap-session]]
        [ring.middleware.session.cookie :only [cookie-store]]
        [shoppinglist.auth :only [authorized? has-permission? is-owner?
                                  wrap-auth authenticate whoami login logout]])
  (:require 
    [shoppinglist.controller :as c]
    [shoppinglist.utils :as u]
    [shoppinglist.model :as model]))

(defroutes routes
  (GET "/lists" req 
       (authorized? req [[has-permission? "list-view-all"]] c/shopping-lists))

  (GET "/lists/:user" req 
       (authorized? req [[is-owner?] [has-permission? "list-view"]] c/shopping-list))
  (PUT "/lists/:user" req
       (authorized? req [[is-owner?] [has-permission? "list-edit"]] c/shopping-list-update))

  (POST "/login" req login)
  (POST "/register" req c/register)
  (GET "/register/:id" req c/accept)
  (GET "/logout" req logout)
  (GET "/whoami" req whoami)

  (GET "/" req {:status 302 :headers {"Location" "/public/index.html"}})
           
  (route/resources "/public")

  (ANY "*" {uri :uri} (u/response-not-found (str "resource not found: " uri)))
  (route/not-found "Not Found"))

(def app
  (-> routes
    (wrap-auth authenticate [#"^/login$" #"^/register/?.*" #"^/public/.*" #"^/$"] )
    (wrap-params)
    (wrap-nested-params)
    (wrap-keyword-params)
    (wrap-session {:store (cookie-store {:key "ABCDEFGHABCDEFGA"})
                   :cookie-name "sid"
                   :cookie-attrs {:max-age 3600}})
    (wrap-cookies)))

(defn init []
  (model/initialize (System/getenv "DSN")))

(defn -main [port]
  (init)
  (run-jetty app {:port (Integer. port) :join? false}))
