(ns bsless.fast.csv.bench
  (:require
   [criterium.core :as cc]
   [clj-async-profiler.core :as prof]
   [bsless.fast.csv :as fast.csv]
   [clojure.data.csv :as csv]))

(defn some-char
  []
  (let [p (fast.csv/some-char-pred "bcdefghijklmnopqrstuvwxyz1289057-=+{}")]
    (-> "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaf"
        (fast.csv/some-char p)
        cc/bench)))

(defn profile-some-char
  []
  (let [p (fast.csv/some-char-pred "bcdefghijklmnopqrstuvwxyz1289057-=+{}")]
    (prof/profile
     (time
      (dotimes [_ 2e8]
        (fast.csv/some-char "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaf" p))))))

(defn write
  [{:keys [rows columns width]
    :or {rows 1000
         columns 10
         width 10}}]
  (let [xs (vec (for [_ (range rows)]
                  (mapv #(apply str (repeat width %)) (range columns))))]
    (println "Bench write csv with" {:rows rows :columns columns :width width})
    (println "data.csv")
    (cc/quick-bench
     (let [w (fast.csv/write-csv-rf)]
       (csv/write-csv w xs)))
    (println "fast.csv")
    (cc/quick-bench
     (transduce (fast.csv/format-csv-xf) fast.csv/write-csv-rf xs))))
