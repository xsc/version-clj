(ns version-clj.core
  (:require [version-clj.split :as sp]
            [version-clj.compare :as c]))

;; ## Facade

(def version->seq
  "Convert version string to version seq (a pair of version/qualifiers) by
   using `.`, `-` and integer/letter changes to detect version parts."
  sp/version->seq)

(def version-seq-compare
  "Compare two version seqs."
  c/version-seq-compare)

(def version-compare
  "Compare two version strings."
  c/version-compare)

;; ## Sorting & Comparison

(defn version-sort
  "Sort a seq of version strings."
  [versions]
  (sort version-compare versions))

(defn version-seq-sort
  "Sort a seq of version seqs."
  [version-seqs]
  (sort version-seq-compare version-seqs))

;; ## Analysis

(defn- to-version-seq
  [v]
  (if (string? v)
    (version->seq v)
    (seq v)))

(defn version-data
  "Get version data from version seq."
  [v]
  (first (to-version-seq v)))

(defn qualifier-data
  "Get qualifier data from version seq."
  [v]
  (second (to-version-seq v)))

(defn snapshot?
  "Check if the given version (string or seq) represents a SNAPSHOT."
  [v]
  (some
    (fn [x]
      (cond (integer? x) false
            (string? x) (= x "snapshot")
            :else (snapshot? x)))
    (to-version-seq v)))

(def qualified?
  "Check if the given version (string or seq) represents a qualified version."
  (letfn [(check-seq [sq]
            (some
              (fn [x]
                (cond (integer? x) false
                      (string? x) true
                      :else (check-seq x)))
              sq))]
    (fn [v]
      (let [v' (to-version-seq v)]
        (or (snapshot? v')
            (check-seq (qualifier-data v')))))))
