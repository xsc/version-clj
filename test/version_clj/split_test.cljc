(ns version-clj.split-test
  (:require #?(:clj [clojure.test :refer [deftest are is]]
               :cljs [cljs.test :refer-macros [deftest are is]])
            [version-clj.split :refer [version->seq]]))

(deftest t-split
  (are [version v] (= v (version->seq version))
       "1.0.0"          [[1 0 0]]
       "1.0"            [[1 0]]
       "1"              [[1]]
       "1a"             [[1] ["a"]]
       "1-a"            [[1] ["a"]]
       "1.0.1-SNAPSHOT" [[1 0 1] ["snapshot"]]
       "1.0.1-alpha2"   [[1 0 1] ["alpha" 2]]
       "11.2.0.3.0"     [[11 2 0 3 0]]
       "1.0-1-0.2-RC"   [[1 [0 1 0] 2] ["rc"]]
       "alpha"          [["alpha"]]
       "alpha-2"        [["alpha"] [2]]
       "1.alpha"        [[1 "alpha"]]
       "1.alpha.2"      [[1 "alpha" 2]]
       "1-alpha.2"      [[1] ["alpha" 2]]
       "0.5.0-alpha.1"  [[0 5 0] ["alpha" 1]]
       ))

(deftest t-split-with-large-number
  (is (= (version->seq "0.0.1-20141002100138")
         [[0 0 1] [20141002100138]]))
  #?(:clj
     (let [v (str Long/MAX_VALUE "12345")]
       (is (= (version->seq v) [[(bigint v)]])))))
