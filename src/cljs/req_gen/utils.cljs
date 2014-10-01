(ns req-gen.helpers)

(defn p [msg] (.log js/console msg))

(defn pclj [msg] (p (clj->js msg)))
