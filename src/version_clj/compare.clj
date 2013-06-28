(ns ^{ :doc "Version Seq Comparison"
       :author "Yannick Scherer" }
  version-clj.compare
  (:use [version-clj.split :only [version-split]]))

;; ## Concept
;;
;; Version vectors can be recursively compared element by element adding
;; zeros at the end if vectors have different lengths.
;;
;; Identical:
;; 
;; - `[(1 0) ("SNAPSHOT")]`
;; - `[(1 0 0) ("SNAPSHOT")]`
;;
;; Lower:
;;
;; - `[(1)]` vs. `[(2)]`
;; - `[(1) ("SNAPSHOT")]` vs. `[(1 0 0) ("SNAPSHOT" 2)]`
;; - `[(1 0) ("SNAPSHOT")]` vs. `[(1) ("final")]`
;;
;; So what we need are comparators for integers, strings and
;; well-known qualifiers.

(defmulti version-element-compare
  (letfn [(f [x]
            (cond (integer? x) :int
                  (string? x) :str
                  (nil? x) :nil
                  :else :lst))] 
    (fn [e0 e1]
      (vector (f e0) (f e1)))))

;; ### List Comparison

(defmethod version-element-compare [:lst :lst]
  [v0 v1]
  (let [v0* (if (< (count v0) (count v1)) (concat v0 (repeat nil)) v0)
        v1* (if (< (count v1) (count v0)) (concat v1 (repeat nil)) v1)]
    (or
      (some
        (fn [[e0 e1]]
          (let [r (version-element-compare e0 e1)]
            (when-not (zero? r) r)))
        (map vector v0* v1*))
      0)))

(defmethod version-element-compare [:lst :nil]
  [v0 _]
  (version-element-compare v0 (repeat (count v0) nil)))

(defmethod version-element-compare [:nil :lst]
  [_ v1]
  (version-element-compare (repeat (count v1) nil) v1))

;; ### Integer Comparison

(defmethod version-element-compare [:int :int] [i0 i1] (compare i0 i1))
(defmethod version-element-compare [:int :nil] [i0 _] (if (zero? i0) 0 1))
(defmethod version-element-compare [:nil :int] [_ i1] (if (zero? i1) 0 -1))
(defmethod version-element-compare [:int :lst] [i0 v0] (version-element-compare [i0] v0))
(defmethod version-element-compare [:lst :int] [v0 i0] (version-element-compare v0 [i0]))
(defmethod version-element-compare [:int :str] [_ _] 1)
(defmethod version-element-compare [:str :int] [_ _] -1)

;; ### String Comparison

(def ^:private QUALIFIERS
  "Order Map for well-known Qualifiers."
  { "alpha"     0 "a"         0
    "beta"      1 "b"         1
    "milestone" 2 "m"         2
    "rc"        3 "cr"        3
    "snapshot"  5
    ""          6 "final"     6 "stable"    6 })

(defmethod version-element-compare [:str :lst] [s0 v0] (version-element-compare [s0] v0))
(defmethod version-element-compare [:lst :str] [v0 s0] (version-element-compare v0 [s0]))
(defmethod version-element-compare [:str :nil] [s0 _] (version-element-compare s0 ""))
(defmethod version-element-compare [:nil :str] [_ s0] (version-element-compare "" s0))
(defmethod version-element-compare [:str :str]
  [s0 s1]
  (let [m0 (get QUALIFIERS s0)
        m1 (get QUALIFIERS s1)]
    (cond (and m0 m1) (compare m0 m1)
          m0 1
          m1 -1
          :else (compare s0 s1))))

;; ## Wrappers

(defn version-compare
  "Compare two version vectors."
  [v0 v1]
  (let [r (version-element-compare v0 v1)]
    (cond (pos? r) 1
          (neg? r) -1
          :else 0)))

(defn version-string-compare
  "Compare two Strings, using the default versioning scheme."
  [s0 s1]
  (version-compare
    (version-split s0)
    (version-split s1)))
