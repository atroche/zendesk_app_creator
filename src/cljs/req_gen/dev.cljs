(ns req-gen.dev
    (:require [figwheel.client :as figwheel :include-macros true]
              [clojure.browser.repl :as repl]
              [cljs.core.async :refer [chan put!]]))

(def is-dev? (.contains (.. js/document -body -classList) "is-dev"))

(defonce re-render-ch (chan))

(when is-dev?
  (enable-console-print!)
  (figwheel/watch-and-reload
   :websocket-url "ws://localhost:3449/figwheel-ws"
   :jsload-callback (fn []
                      (put! re-render-ch true)
                      (print "reloaded"))))
