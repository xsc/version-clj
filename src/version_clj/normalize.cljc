(ns ^:no-doc version-clj.normalize)

;; ## Normalization
;;
;; - Convert Strings to Integer
;; - Replace one-element seqs with its element

(defmulti normalize-element
  "Normalize an Element by class."
  (fn [v]
    (cond (string? v) :string
          (vector? v) :vector
          (seq? v) :seq)))

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

(defn normalize-version-seq
  "Normalize a version seq, creating a seq again."
  [x]
  (let [r (normalize-element x)]
    (if (seq? r) r (vector r))))

