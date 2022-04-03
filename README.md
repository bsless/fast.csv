# io.github.bsless/fast.csv

Fast implementation of CSV writing in Clojure, for when you need to
write big files fast.

## Usage

```clojure
(require '[bsless.fast.csv :as csv])
```

The user level API consists of two functions:

- `csv/format-csv-xf` - which receives the same options as `clojure.data.csv/write-csv`
- `csv/write-csv-rf` - A reducing function which creates a writer, writes to it, and closes it at the end.

For example:

```clojure
(str (transduce (csv/format-csv-xf) csv/write-csv-rf xs))
```

Will serialize `xs` to a string in CSV format.

Since `format-csv-xf` is a transducer, it can be used to stream the
serialization to any sink given a writing reducing function.

If you implement your own reducing function, take into account the
elements can be `String`s or `Integer`s:

```clojure
(if (instance? String o)
  (.write w ^String o)
  (if (instance? Integer o)
    (.write w (.intValue ^Number o))))
```

### Running benchmarks

```bash
clojure -X:dev bsless.fast.csv.bench/write
```
```
Bench write csv with {:rows 1000, :columns 10, :width 10}
data.csv
Evaluation count : 132 in 6 samples of 22 calls.
             Execution time mean : 4.811468 ms
    Execution time std-deviation : 406.708407 µs
   Execution time lower quantile : 4.576434 ms ( 2.5%)
   Execution time upper quantile : 5.500266 ms (97.5%)
                   Overhead used : 2.073452 ns

Found 1 outliers in 6 samples (16.6667 %)
        low-severe       1 (16.6667 %)
 Variance from outliers : 15.6543 % Variance is moderately inflated by outliers
fast.csv
Evaluation count : 2070 in 6 samples of 345 calls.
             Execution time mean : 301.655655 µs
    Execution time std-deviation : 20.448373 µs
   Execution time lower quantile : 288.033701 µs ( 2.5%)
   Execution time upper quantile : 335.490614 µs (97.5%)
                   Overhead used : 2.073452 ns

Found 1 outliers in 6 samples (16.6667 %)
        low-severe       1 (16.6667 %)
 Variance from outliers : 15.0924 % Variance is moderately inflated by outliers
```

## License

Copyright © 2022 Ben Sless

Distributed under the Eclipse Public License version 1.0.
