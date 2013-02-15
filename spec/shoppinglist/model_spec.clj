(ns shoppinglist.model_spec
  (:use [speclj.core]
        [shoppinglist.model])
  (:import
    [org.joda.time DateTime]))

(describe "check-quantity"
  (it "null"
    (should= nil (check-quantity nil)))
  (it "integer"
    (should= 3 (check-quantity 3)))
  (it "float"
    (should= 1.2 (check-quantity 1.2)))
  (it "string"
    (should-throw (check-quantity "abc"))))

(describe "check-item"
  (it "string"
    (should= "test" (check-item "test")))
  (it "not string"
    (should-throw (check-item nil))))

(describe "check-unit"
  (it "null"
    (should= nil (check-unit nil)))
  (it "string"
    (should= "test" (check-unit "test")))
  (it "not string"
    (should-throw (check-unit []))))

(describe "check-priority"
  (it "null"
    (should= nil (check-priority nil)))
  (it "integer"
    (should= 1 (check-priority 1)))
  (it "string good"
    (should= 10 (check-priority "high")))
  (it "string upper good"
    (should= 5 (check-priority "MEDIUM")))
  (it "string strange case good"
    (should= 1 (check-priority "LoW")))
  (it "string bad"
    (should-throw (check-priority "a string")))
  (it "not string"
    (should-throw (check-priority []))))

(describe "new-shoppinglist-item"
  (it "item"
    (should= {:item "an item" :quantity nil :unit nil :priority nil}
             (select-keys (new-shoppinglist-item "an item") [:item :quantity :unit :priority]))
    (should-not (nil? (:inserted (new-shoppinglist-item "an item"))))
    (should (.isBefore (DateTime. (:inserted (new-shoppinglist-item "an item"))) (DateTime/now)))
    (should= nil (:last-bought (new-shoppinglist-item "an item")))))

(run-specs)
