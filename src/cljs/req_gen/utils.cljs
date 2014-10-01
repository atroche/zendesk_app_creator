(ns req-gen.utils)

(defn p [msg] (.log js/console msg))

(defn pclj [msg] (p (clj->js msg)))

(defn hyphenated-name-to-camel-case-name [^String method-name]
  (clojure.string/replace method-name #"-(\w)"
                          #(clojure.string/upper-case (second %1))))


(defn keys-to-camel-case [data]
  (into {}
        (for [[k v] data]
          [(hyphenated-name-to-camel-case-name (name k)) v])))
