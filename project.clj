(defproject version-clj "1.0.1-SNAPSHOT"
  :description "Version Analysis and Comparison for Clojure"
  :url "https://github.com/xsc/version-clj"
  :license {:name "MIT"
            :url "https://choosealicense.com/licenses/mit"
            :year 2013
            :key "mit"
            :comment "MIT License"}
  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [org.clojure/clojurescript "1.10.773" :scope "provided"]
                 [com.google.code.findbugs/jsr305 "3.0.2" :scope "provided"]]

  :profiles {:kaocha
             {:dependencies [[lambdaisland/kaocha "1.0.732"
                              :exclusions [org.clojure/spec.alpha]]
                             [lambdaisland/kaocha-cljs "0.0-71"
                              :exclusions [org.clojure/clojurescript
                                           com.cognitect/transit-clj
                                           com.cognitect/transit-java]]
                             [lambdaisland/kaocha-cloverage "1.0.75"]
                             [org.clojure/tools.namespace "0.3.1"]
                             [org.clojure/java.classpath "1.0.0"]]}
             :ci
             [:kaocha
              {:global-vars {*warn-on-reflection* false}}]}

  :cljsbuild {:test-commands {"node" ["node" :node-runner "target/testable.js"]}
              :builds [{:source-paths ["target/classes" "target/test-classes"]
                        :compiler {:output-to "target/testable.js"
                                   :optimizations :simple}}]}

  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]
            "ci"     ["with-profile" "+ci" "run" "-m" "kaocha.runner"
                      "--reporter" "documentation"
                      "--plugin"   "cloverage"
                      "--codecov"
                      "--no-cov-html"]}

  :pedantic? :abort)
