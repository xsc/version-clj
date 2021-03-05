(ns version-clj.core
  (:require [version-clj.split :as sp]
            [version-clj.compare :as c]
            [version-clj.qualifiers :refer [default-qualifiers]]))

;; ## Facade

(defn version->seq
  "Convert version string to version seq (a pair of version/qualifiers) by
   using `.`, `-` and integer/letter changes to detect version parts."
  #_{:clj-kondo/ignore [:unused-binding]}
  [version & [{:keys [qualifiers]
               :or {qualifiers default-qualifiers}
               :as opts}]]
  (sp/version->seq version opts))

(defn version-seq-compare
  "Compare two sequences created by `version->seq`; returns `-1`, `0` or `1`."
  #_{:clj-kondo/ignore [:unused-binding]}
  [a b & [{:keys [qualifiers]
           :or {qualifiers default-qualifiers}
           :as opts}]]
  (c/version-seq-compare a b opts))

(defn version-compare
  "Compare two version strings; returns `-1`, `0` or `1`."
  #_{:clj-kondo/ignore [:unused-binding]}
  [a b & [{:keys [qualifiers]
           :or {qualifiers default-qualifiers}
           :as opts}]]
  (c/version-compare a b opts))

;; ## Comparison

(defn older?
  "Check whether the given version string is older than the other candidate."
  [a b & [opts]]
  (neg? (version-compare a b opts)))

(defn newer?
  "Check whether the given version string is newer than the other candidate."
  [a b & [opts]]
  (pos? (version-compare a b opts)))

(defn older-or-equal?
  "Check whether the given version string is older than or equal to the other
   candidate."
  [a b & [opts]]
  (not (newer? a b opts)))

(defn newer-or-equal?
  "Check whether the given version string is newer than or equal to the other
   candidate."
  [a b & [opts]]
  (not (older? a b opts)))

;; ## Sorting

(defn version-sort
  "Sort a seq of version strings."
  [versions]
  (sort version-compare versions))

(defn version-seq-sort
  "Sort a seq of version seqs."
  [version-seqs]
  (sort version-seq-compare version-seqs))

;; ## Analysis

(defn- parse-version-seq
  [version opts]
  (if (string? version)
    (version->seq version opts)
    (seq version)))

(defn- qualifiers-of
  [[_ qualifier]]
  (some->> qualifier
           (tree-seq sequential? identity)
           (filter string?)))

(defn parse
  "Parse version into a map of:

   - `:version`: a sequence representing the parsed version,
   - `:qualifiers`: a set containing all string qualifiers,
   - `:snapshot?`: true if version represents a snapshot,
   - `:qualified?`: true if version is qualified.
  "
  [version & [opts]]
  (let [vs (parse-version-seq version opts)
        qs (set (qualifiers-of vs))]
    {:version    vs
     :qualifiers qs
     :snapshot?  (contains? qs "snapshot")
     :qualified? (boolean (seq qs))}))

(defn version-data
  "Get version data from version."
  [version & [opts]]
  (first (parse-version-seq version opts)))

(defn qualifier-data
  "Get qualifier data from version."
  [version & [opts]]
  (second (parse-version-seq version opts)))

(defn snapshot?
  "Check if the given version (string or seq) represents a SNAPSHOT."
  [version & [opts]]
  (->> (parse-version-seq version opts)
       (qualifiers-of)
       (some #{"snapshot"})
       (some?)))

(defn qualified?
  "Check if the given version (string or seq) represents a qualified version."
  [version & [opts]]
  (-> (parse-version-seq version opts)
      (qualifiers-of)
      (seq)
      (boolean)))
