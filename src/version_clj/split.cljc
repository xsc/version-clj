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

;; ## Variants

(def ^:private split-point-variants
  {:default    [SPLIT-DOT SPLIT-DASH SPLIT-COMPOUND]})

(defn- to-split-points
  [value]
  (if (keyword? value)
    (or (get split-point-variants value)
        (get split-point-variants :default))
    value))

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

(letfn [(split* [version]
          (if-let [candidate (last version)]
            (cond (seq? candidate)
                  (if-let [[version' candidate] (split* candidate)]
                    [(concat (butlast version) version') candidate])

                  (and (string? candidate)
                       (re-matches #"[a-zA-Z]+" candidate))
                  [(butlast version) candidate])))]
  (defn- find-qualifier
    [version]
    ;; We should not move things if they are a substantial part of the
    ;; version, e.g. in "alpha" or "1.alpha.2". That means, there needs
    ;; to be at least one dot and a dash in the last part of the version.
    (if (next (last version))
      (split* version))))

(defn- move-qualifier-from-version
  "In some cases, there is an alphanumerical modifier in the version part of
   the sequence, which needs to be moved into the qualifier part. E.g.:

       \"0.5.0-alpha.1\"
       -> r0: ((\"0\") (\"5\") (\"0\" \"alpha\"))
       -> r1: (\"1\")

   We need to find the innermost last element of r0 and move it into r1, iff
   it is not numeric.

   ATTENTION: This is fragile and won't work e.g. for `*-alpha.1.2`."
  [version qualifier]
  (if-let [[version' candidate] (find-qualifier version)]
    [version' (cons candidate qualifier)]))

(defn- combine-with-qualifier
  "After the first split, we might have part of the version in
   the qualifier portion, e.g. `1.0.1-alpha` results in a qualifier of
   `[(\"1\") (\"alpha\")]`.

   Thus, we have to account for that and move the first part of the
   qualifier back into the version."
  [version [fq & rq :as qualifier]]
  (if fq
    (cond-> [(concat version [fq])]
      rq (conj rq))))

(defn version->seq
  "Split version string using the given split points, creating a two-element vector
   representing a version/qualifiers pair."
  ([^String s] (version->seq :default s))
  ([split-points ^String s]
   (let [split-points (to-split-points split-points)]
     (if-not (seq split-points)
       (vector s)
       (let [[p & rst] split-points
             [v0 v1] (first-split-at-point p s)
             version (map #(rest-split-at-points rst %) v0)
             qualifier (rest-split-at-points rst v1)]
         (->> (or (move-qualifier-from-version version qualifier)
                  (combine-with-qualifier version qualifier)
                  [version])
              (mapv normalize-version-seq)))))))
