(ns bsless.fast.csv-test
  (:require
   [clojure.test :as t]
   [bsless.fast.csv :as sut]
   [clojure.data.csv :as csv]))

(def ^{:private true} simple
  "Year,Make,Model
1997,Ford,E350
2000,Mercury,Cougar
")

(def ^{:private true} simple-alt-sep
  "Year;Make;Model
1997;Ford;E350
2000;Mercury;Cougar
")

(def ^{:private true} complicated
  "1997,Ford,E350,\"ac, abs, moon\",3000.00
1999,Chevy,\"Venture \"\"Extended Edition\"\"\",\"\",4900.00
1999,Chevy,\"Venture \"\"Extended Edition, Very Large\"\"\",\"\",5000.00
1996,Jeep,Grand Cherokee,\"MUST SELL!
air, moon roof, loaded\",4799.00")

(t/deftest write
  (t/is (= simple
           (->> simple
                csv/read-csv
                (transduce (sut/format-csv-xf) sut/write-csv-rf)
                str)))
  (t/is (= simple-alt-sep
           (->> (csv/read-csv simple-alt-sep :separator \;)
                (transduce (sut/format-csv-xf :separator \;) sut/write-csv-rf)
                str)))
  (t/is (= (let [w (sut/write-csv-rf)]
             (->> complicated csv/read-csv (csv/write-csv w))
             (str w))
           (->> complicated
                csv/read-csv
                (transduce (sut/format-csv-xf) sut/write-csv-rf)
                str))))
