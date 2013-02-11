(defproject shoppinglist "0.1.0-SNAPSHOT"
  :description "shopping list manager"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [postgresql "9.1-901.jdbc4"]
                 [cheshire "5.0.1"]
                 [ring/ring-core "1.1.8"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [compojure "1.1.3"]
                 [slingshot "0.10.3"]]

  :main shoppinglist.core

  :ring {:handler shoppinglist.core/app, :init shoppinglist.core/init}

  :plugins [[lein-ring "0.8.2"]])
