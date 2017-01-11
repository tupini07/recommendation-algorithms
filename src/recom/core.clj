(ns recom.core
  (:require [recom.apimock :refer :all]
            [recom.recom-algs :as algs]
            [recom.ranking :as r])
  (:gen-class))

(def meid "25124353") ;My user ID for goodreads

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (algs/pearson-correlation-score db "claudia" "michael")
  (algs/euclidean-distance-score db "lisa" "gene")
  (algs/pearson-correlation-score db :john :alys)
  (r/rankings db "toby" algs/pearson-correlation-score)
  (r/rankings db "toby")

  )
