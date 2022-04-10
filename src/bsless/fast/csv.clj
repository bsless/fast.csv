(ns bsless.fast.csv
  (:import
   (java.io Writer StringWriter)))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

;; Writing

(defn escape
  "Escape a string `s` mapping char `from` to string `to` and quote it with `q`.
  Internal implementation detail."
  ^String [^CharSequence s ^Character from ^String to ^Character q]
  (let [from (.charValue from)
        q (.charValue q)]
    (loop [index 0
           buffer (doto (StringBuilder. (.length s)) (.append q))]
      (let [i (unchecked-int index)]
        (if (= (.length s) index)
          (.toString (doto buffer (.append q)))
          (let [ch (.charAt s i)]
            (.append buffer (if (= ch from) to ch))
            (recur (unchecked-inc index) buffer)))))))

(defn format-cell-xf
  "Transducer for formatting CSV cells.
  Receives `quote` char, `quote?` predicate, and the `escaped-quote` to
  quote a cell which needs quoting with.
  Internal implementation detail."
  [quote quote? escaped-quote] ;; (str quote quote)
  (fn -format-cell-xf [rf]
    (fn format-cell-rf
      ([] (rf))
      ([result] (rf result))
      ([result ^Object input]
       (let [string (if (nil? input) "" (. input (toString)))]
         (rf result (if (boolean (quote? string))
                      (escape string quote escaped-quote quote)
                      string)))))))

(comment (escape "abcde" \d "Z" \*))

(defn some-char-pred
  "Takes a coll of chars `chs` and returns a pred which efficiently checks
  if an input char is among them."
  [chs]
  (let [points (mapv int chs)
        min (long (apply min points))
        max (long (apply max points))
        len (inc max)
        arr (boolean-array len)]
    (doseq [point points]
      (aset arr point true))
    (fn pred [^Character ch]
      (let [ch (.charValue ch)
            l (unchecked-long (unchecked-int ch))]
        (when (<= l max)
          (when (<= min l)
            (aget arr (unchecked-int ch))))))))

(defn some-char
  "Check if some char in string `s` satisfies `pred`."
  [^String s pred]
  (let [len (.length s)]
    (loop [i 0]
      (if (= i len)
        false
        (if (pred (.charAt s (unchecked-int i)))
          true
          (recur (unchecked-inc i)))))))

(comment
  (some-char "abcde" (some-char-pred "a"))
  (some-char "abcde" (some-char-pred "b"))
  (some-char "abcde" (some-char-pred "e"))
  (some-char "abcde" (some-char-pred "f")))

(defn format-line-xf
  "Transducer for formatting CSV lines.
  Internal implementation detail."
  [opts]
  (let [separator (int (or (:separator opts) \,))
        quote (or (:quote opts) \")
        pred (some-char-pred #{separator quote \return \newline})
        quote? (or (:quote? opts) #(some-char % pred))]
    (comp (format-cell-xf quote quote? (str quote quote)) (interpose separator))))

(comment
  (transduce (format-line-xf {}) conj [1 2 3])
  (transduce (format-line-xf {}) conj ["ac, abs, moon"])
  (transduce (format-line-xf {}) conj ["Venture \"Extended Edition, Very Large\""]))

(defn append-xf
  "Transducer which appends `sep` at the end of a sequence.
  Internal implementation detail. Used to add newline character at the
  end of each row."
  [sep]
   (fn -append-sep-xf [rf]
     (fn append-rf
       ([] (rf))
       ([result] (rf (rf result sep)))
       ([result input] (rf result input)))))

(defn format-csv-xf
  "Writes data to writer in CSV-format.
   Valid options are
     :separator (Default \\,)
     :quote (Default \\\")
     :quote? (A predicate function which determines if a string should be quoted. Defaults to quoting only when necessary.)
     :newline (:lf (default) or :cr+lf)"
  [& options]
  (let [opts (apply hash-map options)
        newline (or (:newline opts) :lf)
        newline ({:lf "\n" :cr+lf "\r\n"} newline)
        line-xf (format-line-xf opts)
        row-xf (comp line-xf (append-xf newline))]
    (mapcat (fn [line] (->Eduction row-xf line)))))

(defn write-csv-rf
  (^Writer [] (StringWriter.))
  (^Writer [^Writer w] (.close w) w)
  (^Writer [^Writer w o]
   (if (instance? String o)
     (.write w ^String o)
     (when (instance? Integer o)
       (.write w (.intValue ^Number o))))
   w))
