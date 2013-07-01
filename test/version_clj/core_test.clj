(ns ^{:doc "Tests for Core Namespace" 
      :author "Yannick Scherer"}
  version-clj.core-test
  (:use midje.sweet
        version-clj.core))

(tabular
  (fact "about SNAPSHOT detection"
    (snapshot? ?v) => ?r)
  ?v                       ?r
  "1.0.0"                  falsey
  "SNAPSHOT"               truthy
  "1-SNAPSHOT"             truthy
  "1.0-SNAPSHOT"           truthy
  "1.0-SNAPSHOT.2"         truthy
  "1.0-NOSNAPSHOT"         falsey)

(tabular
  (fact "about qualifier detection"
    (qualified? ?v) => ?r)
  ?v                       ?r
  "1.0.0"                  falsey
  "SNAPSHOT"               truthy
  "1-SNAPSHOT"             truthy
  "1.0-SNAPSHOT"           truthy
  "1.0-SNAPSHOT.2"         truthy
  "1.0-NOSNAPSHOT"         truthy
  "1.x.2"                  falsey
  "1.2y"                   truthy
  "1.y2"                   falsey
  "1.y"                    falsey)
