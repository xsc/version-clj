(defproject version-clj "0.1.0"
  :description "Version Analysis and Comparison for Clojure"
  :url "https://github.com/xsc/version-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :source-paths ["src/cljx"]
  :test-paths ["target/test-classes"]

  :profiles {:dev {:dependencies [[com.cemerick/clojurescript.test "0.3.3"]]
                   :plugins [[org.clojure/clojurescript "0.0-2665"]
                             [com.keminglabs/cljx "0.5.0"]
                             [lein-cljsbuild "1.0.4"]]}}

  :prep-tasks [["cljx" "once"]]
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}
                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}
                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :cljs}]}
  :jar-exclusions [#"\.cljx"])
