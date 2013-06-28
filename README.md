# version-clj

__version-clj__ is a Clojure library for analysis and comparison of artifact version numbers.

[![Build Status](https://travis-ci.org/xsc/version-clj.png?branch=master)](https://travis-ci.org/xsc/version-clj)
[![endorse](https://api.coderwall.com/xsc/endorsecount.png)](https://coderwall.com/xsc)

It originated as a comparison mechanism in [lein-ancient](https://github.com/xsc/lein-ancient), a plugin to detect
outdated dependencies in your packages.

## Usage

__Leiningen__ ([via Clojars](https://clojars.org/version-clj))

Put the following into the `:dependencies` vector of your `project.clj`.

```clojure
[version-clj "0.1.0-SNAPSHOT"]
```

__REPL__

```clojure
(use 'version-clj.core)

(version->seq "1.0.0-SNAPSHOT")
;; => [(1 0 0) ("snapshot")]
(version->seq "9.1-0-1.1-jdbc4")
;; => [(9 (1 0 1) 1) ("jdbc" 4)]

(version-compare "1.0" "1.0.0")
;; => 0
(version-compare "1.0-alpha5" "1.0-alpha14")
;; => -1
(version-compare "1.0-milestone" "1.0.0-final")
;; => -1
```

## Version Sequence Creation

A version seq is a pair of version-information and qualifier-information. In general, a version string is split using
dots (`.`) and dashes (`-`), but it is assumed that the last result of the dot-split represents qualifiers (except for its 
first element which is put into the version data seq). For example, the algorithm might produce the following steps:

```clojure
   "9.1-0-1.1-alpha4"
=> ("9" "1-0-1" "1-alpha4")                                ;; split by dots
=> (("9" "1-0-1") ("1-alpha4"))                            ;; group into version/qualifier data
=> ((("9") ("1" "0" "1")) (("1" "alpha4")))                ;; split by dashes
=> (((("9")) (("1") ("0") ("1"))) ((("1") ("alpha" "4")))) ;; split by letter/integer changes
=> ((9 (1 0 1)) (1 ("alpha" 4)))                           ;; normalize
=> ((9 (1 0 1) 1) (("alpha" 4)))                           ;; rearrange remaining version data
=> ((9 (1 0 1) 1) ("alpha" 4))                             ;; normalize qualifiers again
```

This should create results that represent an intuitive reading of version numbers.

## Version Comparison

Version seqs are compared by extending them to the same length (using zero/nil) followed by an element-wise
comparison.

- Integers are compared numerically.
- Strings are compared using a table of well-known qualifiers or lexicographically.
- Subsequences are compared like they represent versions of their own.
- An integer is newer than a string.

## License

Copyright &copy; 2013 Yannick Scherer

Distributed under the Eclipse Public License, the same as Clojure.
