(ns ^{:doc "Core Namespace"
      :author "Yannick Scherer"}
  version-clj.core
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

(defmacro ^:private def-vfn
  "Create Function that automatically converts strings to version seqs and passes them to the given 
   function."
  [id docstring f]
  `(def ~id ~docstring
     (let [f# ~f]
       (fn [v#]
         (if (string? v#)
           (f# (version->seq v#))
           (f# v#))))))

(def-vfn version-data 
  "Get version data from version seq."
  first)

(def-vfn qualifier-data
  "Get qualifier data from version seq."
  second)

(def-vfn snapshot?
  "Check if the given version (string or seq) represents a SNAPSHOT."
  (fn [v]
    (some 
      (fn [x]
        (cond (integer? x) false
              (string? x) (= x "snapshot")
              :else (snapshot? x)))
      v)))

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
      (or (snapshot? v) (check-seq (qualifier-data v))))))
