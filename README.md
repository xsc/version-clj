# version-clj

[![clojars](https://img.shields.io/clojars/v/version-clj.svg)](https://clojars.org/version-clj)
[![Documentation](https://cljdoc.org/badge/xsc/version-clj)](https://cljdoc.org/d/xsc/version-clj/CURRENT)
![CI](https://github.com/xsc/version-clj/workflows/CI/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/xsc/version-clj/branch/master/graph/badge.svg?token=xmrXrhA6Z7)](https://codecov.io/gh/xsc/version-clj)

__version-clj__ is a Clojure &amp; ClojureScript library for analysis and
comparison of artifact version numbers. It originated as a comparison mechanism
in [lein-ancient][], a plugin to detect outdated dependencies in your packages.

[lein-ancient]: https://github.com/xsc/lein-ancient

## Usage

```clojure
(require '[version-clj.core :as v])
```

### Comparison

```clojure
(v/older? "1.0.0-alpha" "1.0.0")     ;; => true
(v/newer? "1.0.0-rc2" "1.0.0-rc1")   ;; => true
(v/version-compare "1.0.0" "0.9.0")  ;; => 1
```

### Sorting

```clojure
(v/version-sort ["1.0.0-alpha", "1.0.0", "1.0.0-SNAPSHOT", "0.9.0-RC1"])
;; => ("0.9.0-RC1" "1.0.0-alpha" "1.0.0-SNAPSHOT" "1.0.0")
```

### Analysis

```clojure
(v/parse "1.0.0-rc-snapshot")
;; => {:version [(1 0 0) ("rc" "snapshot")],
;;     :qualifiers #{"rc" "snapshot"},
;;     :snapshot? true,
;;     :qualified? true}

(v/snapshot? "1.0.0")
;; => false

(v/qualified? "1.0.0-rc")
;; => true
```

## Comparison Rules

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
