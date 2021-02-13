# version-clj

[![clojars](https://img.shields.io/clojars/v/version-clj.svg)](https://clojars.org/version-clj)
![CI](https://github.com/xsc/version-clj/workflows/CI/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/xsc/version-clj/branch/master/graph/badge.svg?token=xmrXrhA6Z7)](https://codecov.io/gh/xsc/version-clj)

__version-clj__ is a Clojure &amp; ClojureScript library for analysis and
comparison of artifact version numbers. It originated as a comparison mechanism
in [lein-ancient][], a plugin to detect outdated dependencies in your packages.

[lein-ancient]: https://github.com/xsc/lein-ancient

## Usage

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

A version seq is a pair of version-information and qualifier-information. In
general, a version string is split using dots (`.`) and dashes (`-`), but it is
assumed that the last result of the dot-split represents qualifiers (except for
its first element which is put into the version data seq). For example, the
algorithm might produce the following steps:

```clojure
   "9.1-0-1.1-alpha4"
=> ("9" "1-0-1" "1-alpha4")                                ;; split by dots
=> (("9" "1-0-1") ("1-alpha4"))                            ;; group into version/qualifier data
=> ((("9") ("1" "0" "1")) (("1" "alpha4")))                ;; split by dashes
=> (((("9")) (("1") ("0") ("1"))) ((("1") ("alpha" "4")))) ;; split by letter/integer changes
=> [(9 (1 0 1)) (1 ("alpha" 4))]                           ;; normalize
=> [(9 (1 0 1) 1) (("alpha" 4))]                           ;; rearrange remaining version data
=> [(9 (1 0 1) 1) ("alpha" 4)]                             ;; normalize qualifiers again
```

This should create results that represent an intuitive reading of version
numbers.

## Version Comparison

Version seqs are compared by extending them to the same length (using zero/nil)
followed by an element-wise comparison.

- Integers are compared numerically.
- Strings are compared using a table of well-known qualifiers or lexicographically.
- A well-known qualifier is newer than an unknown one.
- Subsequences are compared like they represent versions of their own.
- An integer is newer than a string.

The order of well-known qualifiers is case-insensitive and given as:

```clojure
  "alpha"     == "a"
< "beta"      == "b"
< "milestone" == "m"
< "rc"        == "cr"
< "snapshot"
< "final"     == "stable" == ""
```

Have a look at the [respective unit tests][compare-test] to see the comparison
mechanism in action.

[compare-test]: https://github.com/xsc/version-clj/blob/master/test/version_clj/compare_test.cljc

## License

```
MIT License

Copyright (c) 2013-2021 Yannick Scherer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
