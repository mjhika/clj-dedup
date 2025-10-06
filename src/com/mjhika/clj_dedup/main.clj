(ns com.mjhika.clj-dedup.main
  (:require
   [charred.api :as charred]
   [clojure.java.io :as io]
   [clojure.pprint :as pp])
  (:import
   (java.io File)
   (java.nio.file Files)
   (java.security MessageDigest)
   (java.text SimpleDateFormat)
   (java.util Date))
  (:gen-class))

(set! *warn-on-reflection* true)

(defn file? [^File s]
  (.isFile s))

(defn directory? [^File s]
  (.isDirectory s))

(defn enum-files [dir]
  (filter file? (file-seq (io/file dir))))

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

(defn inspect [d]
  (pp/pprint d)
  d)

(defn entry->csv-data [[k s]]
  (mapv #(vector % k) s))

(defn timestamp []
  (let [df (new SimpleDateFormat "yyyy-MM-dd'T'HHmmss")]
    (.format df (new Date))))

(defn help []
  (println "clj-dedup - File duplicate discovery and resolution tool")
  (newline)
  (println "SYNOPSIS")
  (println "\tclj-dedup directory")
  (newline)
  (println "DESCRIPTION")
  (println "\tclj-dedup is a utility for finding duplicate files.")
  (println "\tThe only valid usage of this utility is to supply a directory."))

(defn parse-arg [arg]
  (let [f (io/file arg)]
    (when (directory? f) f)))

(defn -main [& args]
  (let [valid-dir (parse-arg (or (first args) "."))
        atm (atom {})]
    (when-not valid-dir
      (help)
      (System/exit 1))
    (->> (enum-files valid-dir)
         (map file-details)
         (#(doseq [tup %]
             (swap! atm assoc-entry tup))))
    (let [dupes (flatten (map seq (vals (filter v>1? @atm))))
          final (atom {})]
      (->> dupes
           (map hash-file)
           (#(doseq [tup %]
               (swap! final assoc-entry tup))))
      (let [csv-data (->> (into {} (filter v>1? @final))
                          inspect
                          (mapv entry->csv-data))
            filename (format "./%s_dupes.csv" (timestamp))]
        (with-open [w (io/writer filename)]
          (charred/write-csv
           w
           (apply concat [["filepath" "sha512-checksum"]] csv-data)))
        (newline)
        (println "See" filename "for a CSV export.")))))

(comment
  (sha512sum (file->bytes "deps.edn"))
  (-main nil))
