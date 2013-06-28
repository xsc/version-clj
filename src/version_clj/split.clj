(ns ^{:doc "Splitting a Version String"
      :author "Yannick Scherer"}
  version-clj.split)

;; ## Concept
;;
;; By using a set of prioritized splitting points, we can create a vector representation
;; of a version string. For example, the version `"1.1-0-1.2-SNAPSHOT"` might result in:
;;
;; - `[1 [1 0 1] [2 "SNAPSHOT"]]`
;; - `[[1 1] 0 [1 2] "SNAPSHOT"]`
;;
;; depending on whether "." has a higher priority than "-" or not.

;; ## Utility

(defn- normalize-version-seq
  "Convert elements of a version seq to integers if possible."
  [v]
  (map
    (fn [x]
      (cond (string? x) (try
                          (Integer/parseInt ^String x)
                          (catch Exception _ x))
            (integer? x) x
            :else (normalize-version-seq x)))
    v))

;; ## Split Points

(defprotocol SplitPoint
  "Protocol for Split Points."
  (split [this ^String s]))

(extend-type java.lang.String
  SplitPoint
  (split [this s]
    (.split ^String s this)))

(extend-type clojure.lang.AFunction
  SplitPoint
  (split [f s]
    (f s)))

;; ## Splitting Algorithm

(defn- split-at-points
  "Split version string at the given split points."
  [split-points ^String s]
  (if-not (seq split-points) s
    (let [[c & rst] split-points
          parts (split c s)]
      (if (= (count parts) 1)
        s
        (map (partial split-at-points rst) parts)))))

(defn version-split
  "Split Version String using the given split points, normalize result."
  [split-points ^String s]
  (let [v (split-at-points split-points s)
        v (if (string? v) [v] v)]
    (normalize-version-seq v)))

;; ## Split Points

(def ^:const SPLIT-DOT  "\\.")
(def ^:const SPLIT-DASH "-")

(defn SPLIT-COMPOUND
  "Split a given string into char-only and int-only parts."
  [^String v]
  (loop [^String v v
         result []]
    (if (seq v)
      (let [[c & rst] v
            split-rx (if (Character/isDigit c) "[^0-9]" "[0-9]")
            split-result (.split v split-rx 2)
            first-part (first split-result)
            rest-part (.substring v (count first-part))]
        (recur rest-part (conj result first-part)))
      result)))
