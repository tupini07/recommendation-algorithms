(defproject r-book "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/tupini07/recommendation-algs"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-oauth "1.5.5"]
                 [clj-http "2.2.0"]]
  :main ^:skip-aot recom.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
