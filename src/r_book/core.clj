(ns r-book.core
  (:require [r-book.wrapper :as w])
  (:gen-class))

(def meid "25124353") ;My user ID



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "My books!")
  (pprint (w/get-user-books meid)))
