(ns build
  (:refer-clojure :exclude [test])
  (:require
   [clojure.tools.build.api :as b]
   [org.corfield.build :as bb]))

(def lib 'io.github.bsless/fast.csv)
(def revs (b/git-count-revs nil))
(def ver "0.0.%s")
(def version (format ver revs))
(def snapshot (format ver revs))

(defn options
  [opts]
  (-> opts
      (assoc :lib lib)
      (assoc :version (if (:snapshot opts) snapshot version))))

(defn test "Run the tests." [opts]
  (bb/run-tests opts))

(defn ci "Run the CI pipeline of tests (and build the JAR)." [opts]
  (-> opts
      (options)
      (bb/run-tests)
      (bb/clean)
      (bb/jar)))

(defn install "Install the JAR locally." [opts]
  (-> opts
      (options)
      (bb/install)))

(defn deploy "Deploy the JAR to Clojars." [opts]
  (-> opts
      (options)
      (bb/deploy)))
