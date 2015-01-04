(ns version-clj.split-test
  (:require [clojure.test :refer :all]
            [version-clj.split :refer :all]))

(deftest t-split
  (are [version v] (= (version->seq version) v)
       "1.0.0"          [[1 0 0]]
       "1.0"            [[1 0]]
       "1"              [[1]]
       "1a"             [[1] ["a"]]
       "1-a"            [[1] ["a"]]
       "1.0.1-SNAPSHOT" [[1 0 1] ["snapshot"]]
       "1.0.1-alpha2"   [[1 0 1] ["alpha" 2]]
       "11.2.0.3.0"     [[11 2 0 3 0]]
       "1.0-1-0.2-RC"   [[1 [0 1 0] 2] ["rc"]]))
