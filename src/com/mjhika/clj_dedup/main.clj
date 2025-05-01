(ns com.mjhika.clj-dedup.main
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :as pp])
  (:import
   (java.security MessageDigest)
   (java.nio.file Files))
  (:gen-class))

(set! *warn-on-reflection* true)

(defn enum-files [dir]
  (filter #(.isFile ^java.io.File %)
          (file-seq (io/file dir))))

(defn file-details [file]
  [(.length (io/file file)) (str file)])

(defn assoc-entry [m [k v]]
  (let [cv (m k)]
    (assoc m k (apply hash-set v cv))))

(defn v>1? [[_ v]]
  (> (count v) 1))

(defn sha512sum [^byte/1 input]
  (let [md (doto (MessageDigest/getInstance "SHA-512") (.update input))]
    (apply str (map #(format "%02x" %) (.digest md)))))

(defn file->bytes [file]
  (Files/readAllBytes (.toPath (io/file file))))

(defn hash-file [file]
  [(sha512sum (file->bytes file)) file])

(defn -main [& args]
  (let [atm (atom {})]
    (->> (enum-files (or (first args) "."))
         (map file-details)
         (#(doseq [tup %]
             (swap! atm assoc-entry tup))))
    (let [dupes (flatten (map seq (vals (filter v>1? @atm))))
          final (atom {})]
      (->> dupes
           (map hash-file)
           (#(doseq [tup %]
               (swap! final assoc-entry tup))))
      (pp/pprint (filter v>1? @final)))))

(comment
  (sha512sum (file->bytes "deps.edn"))
  (-main *command-line-args*))
