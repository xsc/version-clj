(ns ^:no-doc version-clj.split
  (:require [version-clj.normalize :as normalize]
            [version-clj.qualifiers :refer [default-qualifiers]]
            [clojure.string :as string]))

;; ## Desired Results
;;
;; "1.0"                -> ((1 0))
;; "1"                  -> ((1))
;; "1.0-SNAPSHOT"       -> ((1 0) ("SNAPSHOT"))
;; "1-SNAPSHOT"         -> ((1) ("SNAPSHOT"))
;; "1.0-1-0.2-alpha2"   -> ((1 (0 1 0) 2) ("alpha" 2))

;; ## Split Helpers

(defn- split
  "Helper to perform a split operation, either by regex or function."
  [split-point s]
  (string/split s split-point))

(defn- split-once
  "Helper to split exactly once on the given regex."
  [re s]
  #?(:clj  (let [m (re-matcher re s)]
             (if (.find m)
               (let [idx (.start m)
                     len (- (.end m) idx)]
                 [(subs s 0 idx)
                  (subs s (+ idx len))])
               [s]))
     :cljs (if-let [parts (.exec re s)]
             (let [idx (.-index parts)
                   len (count (aget ^array parts 0))]
               [(subs s 0 idx)
                (subs s (+ idx len))])
             [s])))

(defn- split-all
  "Helper to recursively apply split points."
  [split-points s]
  (if-not (seq split-points)
    [s]
    (filter
      (complement empty?)
      (let [[p & rst] split-points
            parts (split p s)]
        (if (= (count parts) 1)
          (split-all rst s)
          (map #(split-all rst %) parts))))))

;; ## Split Points

(def ^:const SPLIT-DOT
  "Split at `.` character."
  #"\.")

(def ^:const SPLIT-DASH
  "Split at `-` character."
  #"-")

(def ^:const SPLIT-COMPOUND
  "Split a given string into char-only and int-only parts."
  #"(?<=\D)(?=\d)|(?<=\d)(?=\D)")

(def ^:const SPLIT-LAST-QUALIFIER
  "Split at the last dash, if followed by a letter or at least 14 numbers
   (indicating a date)."
  #"(?i)-(?=[^\d]|\d{14})")

;; ## Splitting Algorithm

;; ### Splitting a plain version
;;
;;     "1.0-1-x5.1"
;;     -> ("1" "0-1-x" "1")
;;     -> (("1") ("0" "1" "x5") ("1"))
;;     -> ("1" ("0" "1" "x5") "1")
;;     -> ("1" ("0" "1" ("x" "5")) "1")

(defn- split-version
  "Split a version that has already been separated from its qualifier."
  [version]
  (split-all [SPLIT-DOT SPLIT-DASH SPLIT-COMPOUND] version))

;; ### Splitting a plain qualifier
;;
;;     "RC-SNAPSHOT4.1
;;     -> ("RC" "SNAPSHOT4.1")
;;     -> ("RC" ("SNAPSHOT4" "1"))
;;     -> ("RC" (("SNAPSHOT" "4") "1"))

(defn- split-qualifier
  "Split a qualifier that has already been separated from its version."
  [v]
  (split-all [SPLIT-DASH SPLIT-DOT SPLIT-COMPOUND] v))

;; ### Splitting into version/qualifier
;;
;; Either use a well-known qualifier or the last dash.

(def qualifier-regex
  "Create a regex that can split at known qualifiers."
  (memoize
    (fn [qualifiers]
      (let [rx-or (->> (keys qualifiers)
                       (remove empty?)
                       #?(:clj (map #(str "\\Q" % "\\E")))
                       (string/join "|"))]
        (re-pattern
          (str "(?i)(^|(?<=\\d)|[.\\-])(?=("
               rx-or
               ")([\\d.\\-]|$))"))))))

(defn- split-known-qualifier
  "Find one of the known qualifiers and split right before it."
  [qualifiers v]
  (split-once (qualifier-regex qualifiers) v))

(def split-unknown-qualifier
  "Split at `-`, followed by a letter."
  (fn [v]
    (split-once SPLIT-LAST-QUALIFIER v)))

(defn- split-version-and-qualifier
  [v qualifiers]
  (if (empty? qualifiers)
    (split-unknown-qualifier v)
    (let [[version qualifier] (split-known-qualifier qualifiers v)]
      (if qualifier
        [version qualifier]
        (split-unknown-qualifier v)))))

;; ### Full Algorithm
;;
;; 1. Split into version and qualifier
;; 2. Split version
;; 3. Split qualifier
;; 4. Normalise result

(defn version->seq
  "Split version string into a sequence representation with logical grouping."
  [v & [{:keys [qualifiers] :or {qualifiers default-qualifiers}}]]
  (let [[version qualifier] (split-version-and-qualifier v qualifiers)]
    (->> (if qualifier
           (vector
             (split-version version)
             (split-qualifier qualifier))
           [(split-version version)])
         (mapv normalize/normalize-version-seq))))
