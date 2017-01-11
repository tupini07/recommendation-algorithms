(ns r-book.core
  (:require [r-book.apimock :refer :all]
            [r-book.recom-algs :as algs])
  (:gen-class))

(def meid "25124353") ;My user ID for goodreads

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (algs/pearson-correlation-score db "john" "carl")
  (algs/euclidean-distance-score db "john" "carl")
  (algs/pearson-correlation-score db :john :alys)
  )
