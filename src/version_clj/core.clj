(ns ^{:doc "Core Namespace"
      :author "Yannick Scherer"}
  version-clj.core
  (:require [version-clj.split :as sp]
            [version-clj.compare :as c]))

(def version->seq
  "Convert version string to version seq (a pair of version/qualifiers) by 
   using `.` and `-` to detect version parts."
  sp/version->seq)

(def version-seq-compare 
  "Compare two version seqs."
  c/version-seq-compare)

(def version-compare 
  "Compare two version strings."
  c/version-compare)
