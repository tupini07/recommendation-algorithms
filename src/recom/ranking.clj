(ns recom.ranking
  (:require [recom.recom-algs :as algs]))

(defn rankings 
  ([prefs ent1] (rankings prefs ent1 algs/euclidean-distance-score))
  ([prefs ent1 alg] 
   "Compares ent1 to all ents in prefs and returns a sorted list of the most similar ents"
   (->> (keys prefs)
        (filter #(not= % (keyword ent1)))
        (map #(identity {:ent        % 
                         :similarity (alg prefs ent1 %)}))
        (sort-by :similarity)
        (reverse))))
