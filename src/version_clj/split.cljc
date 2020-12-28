(ns version-clj.split
  (:require [clojure.string :as string]))

;; ## Desired Results
;;
;; "1.0"                -> ((1 0))
;; "1"                  -> ((1))
;; "1.0-SNAPSHOT"       -> ((1 0) ("SNAPSHOT"))
;; "1-SNAPSHOT"         -> ((1) ("SNAPSHOT"))
;; "1.0-1-0.2-alpha2"   -> ((1 (0 1 0) 2) ("alpha" 2))

;; ## Normalization
;;
;; - Convert Strings to Integer
;; - Replace one-element seqs with its element

(defmulti normalize-element
  "Normalize an Element by class."
  (fn [v]
    (cond (string? v) :string
          (vector? v) :vector
          (seq? v) :seq))
  :default nil)

(defmethod normalize-element :string
  [#?(:clj ^String x, :cljs ^js/String x)]
  (if (re-matches #"\d+" x)
    #?(:clj
       (try
         (Long/parseLong x)
         (catch NumberFormatException _
           (bigint x)))
       :cljs
       (js/parseFloat x))
    (.toLowerCase x)))

(defmethod normalize-element :seq
  [x]
  (let [r (map normalize-element x)]
    (if (= (count r) 1)
      (first r)
      r)))

(defmethod normalize-element :vector
  [x]
  (normalize-element (seq x)))

(defmethod normalize-element nil
  [x]
  x)

(defn normalize-version-seq
  "Normalize a version seq, creating a seq again."
  [x]
  (let [r (normalize-element x)]
    (if (seq? r) r (vector r))))

;; ## Split Points

(defn- split
  [v s]
  (if (fn? v)
    (v s)
    (string/split s v)))

;; ## Predefined Split Points

(def ^:const SPLIT-DOT
  "Split at `.` character."
  #"\.")

(def ^:const SPLIT-DASH
  "Split at `-` character."
  #"-")

(defn SPLIT-COMPOUND
  "Split a given string into char-only and int-only parts."
  [v]
  (loop [#?(:clj ^String v, :cljs ^js/String v) v
         result []]
    (if (empty? v)
      result
      (let [c (subs v 0 1)
            split-rx (if (re-matches #"\d" c) #"[^0-9]" #"[0-9]")
            split-result (string/split v split-rx 2)
            first-part (first split-result)
            rest-part (subs v (count first-part))]
        (recur rest-part (conj result first-part))))))

;; ## Splitting Algorithm
;;
;; We employ a special treatment of the first element of the last split vector.
;;
;;    "1.0-1-0.1-SNAPSHOT"
;;    -> ("1" "0-1-0" "1-SNAPSHOT")              # split using first split point
;;    -> (("1" "0-1-0") "1-SNAPSHOT")            # group into version/rest
;;    -> (("1" ("0" "1" "0")) ("1" "SNAPSHOT"))  # split using other split points
;;    -> (("1" ("0" "1" "0") "1") ("SNAPSHOT"))  # merge first of rest into version
;;    -> ((1 (0 1 0) 1) ("SNAPSHOT"))            # normalize
;;

(defn- first-split-at-point
  "Split using first split point. Creates a two-element vector consisting of the parts.
   The result should be interpreted as a version/qualifier data pair."
  [first-split-point ^String s]
  (let [parts (split first-split-point s)]
    (if (= (count parts) 1)
      (vector nil s)
      (vector (butlast parts) (last parts)))))

(defn- rest-split-at-points
  "Split version string recursively at the given split points."
  [split-points ^String s]
  (if-not (seq split-points)
    [s]
    (filter
      (complement empty?)
      (let [[p & rst] split-points
            parts (split p s)]
        (if (= (count parts) 1)
          (rest-split-at-points rst s)
          (map #(rest-split-at-points rst %) parts))))))

(defn version->seq
  "Split version string using the given split points, creating a two-element vector
   representing a version/qualifiers pair."
  ([^String s] (version->seq [SPLIT-DOT SPLIT-DASH SPLIT-COMPOUND] s))
  ([split-points ^String s]
   (if-not (seq split-points)
     (vector s)
     (let [[p & rst] split-points
           [v0 v1] (first-split-at-point p s)
           r0 (map #(rest-split-at-points rst %) v0)
           r1 (rest-split-at-points rst v1)]
       (if-let [p (first r1)]
         (let [r0 (normalize-version-seq (concat r0 [p]))
               r1 (normalize-version-seq (rest r1))]
           (if (seq r1)
             (vector r0 r1)
             (vector r0)))
         (vector (normalize-version-seq r0)))))))
