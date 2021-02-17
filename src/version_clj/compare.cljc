(ns ^:no-doc version-clj.compare
  (:require [version-clj.qualifiers :refer [default-qualifiers]]
            [version-clj.split :refer [version->seq]]))

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
    (fn [e0 e1 _qualifiers]
      (vector (f e0) (f e1)))))

;; ### List Comparison

(defmethod version-element-compare [:lst :lst]
  [v0 v1 q]
  (let [v0* (if (< (count v0) (count v1)) (concat v0 (repeat nil)) v0)
        v1* (if (< (count v1) (count v0)) (concat v1 (repeat nil)) v1)]
    (or
      (some
        (fn [[e0 e1]]
          (let [r (version-element-compare e0 e1 q)]
            (when-not (zero? r) r)))
        (map vector v0* v1*))
      0)))

(defmethod version-element-compare [:lst :nil]
  [v0 _ q]
  (version-element-compare v0 (repeat (count v0) nil) q))

(defmethod version-element-compare [:nil :lst]
  [_ v1 q]
  (version-element-compare (repeat (count v1) nil) v1 q))

;; ### Integer Comparison

(defmethod version-element-compare [:int :int] [i0 i1 _] (compare i0 i1))
(defmethod version-element-compare [:int :nil] [i0 _ _] (if (zero? i0) 0 1))
(defmethod version-element-compare [:nil :int] [_ i1 _] (if (zero? i1) 0 -1))
(defmethod version-element-compare [:int :lst] [i0 v0 q] (version-element-compare [i0] v0 q))
(defmethod version-element-compare [:lst :int] [v0 i0 q] (version-element-compare v0 [i0] q))
(defmethod version-element-compare [:int :str] [_ _ _] 1)
(defmethod version-element-compare [:str :int] [_ _ _] -1)

;; ### String Comparison

(defmethod version-element-compare [:str :lst] [s0 v0 q] (version-element-compare [s0] v0 q))
(defmethod version-element-compare [:lst :str] [v0 s0 q] (version-element-compare v0 [s0] q))
(defmethod version-element-compare [:str :nil] [s0 _ q] (version-element-compare s0 "" q))
(defmethod version-element-compare [:nil :str] [_ s0 q] (version-element-compare "" s0 q))
(defmethod version-element-compare [:str :str]
  [s0 s1 qualifiers]
  (let [m0 (get qualifiers s0)
        m1 (get qualifiers s1)]
    (cond (and m0 m1) (compare m0 m1)
          m0 1
          m1 -1
          :else (compare s0 s1))))

;; ## Wrappers

(defn version-seq-compare
  "Compare two version seqs."
  [v0 v1 & [{:keys [qualifiers] :or {qualifiers default-qualifiers}}]]
  (let [r (version-element-compare v0 v1 qualifiers)]
    (cond (pos? r) 1
          (neg? r) -1
          :else 0)))

(defn version-compare
  "Compare two Strings, using the default versioning scheme."
  [s0 s1 & [{:keys [qualifiers] :or {qualifiers default-qualifiers}}]]
  (version-seq-compare
    (version->seq s0)
    (version->seq s1)
    {:qualifiers qualifiers}))
