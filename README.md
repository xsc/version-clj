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

## License

Copyright &copy; 2013 Yannick Scherer

Distributed under the Eclipse Public License, the same as Clojure.
