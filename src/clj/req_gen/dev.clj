(ns req-gen.dev
  (:require [environ.core :refer [env]]
            [net.cgrand.enlive-html :refer [set-attr prepend append html]]
            [cemerick.piggieback :as piggieback]
            [cemerick.austin]
            [cemerick.austin.repls :refer [browser-connected-repl-js]]
            [weasel.repl.websocket :as weasel]))

(def is-dev? (env :is-dev))
(defn repl-script []
  (html [:script {:type "text/javascript"}
                     (str "window.setTimeout(function(event) {"
                          (browser-connected-repl-js)
                          "}, 1000);")]))

(defn inject-devmode-html []
  (comp
     (set-attr :class "is-dev")
     (prepend (html [:script {:type "text/javascript" :src "/out/goog/base.js"}]))
     (prepend (html [:script {:type "text/javascript" :src "/react/react.js"}]))
     (append (repl-script))
     (append  (html [:script {:type "text/javascript"} "goog.require('req_gen.core')"]))
    ))

(defn start-brepl []
  (reset! cemerick.austin.repls/browser-repl-env
                        (cemerick.austin/repl-env)))

(defn browser-repl []
  (piggieback/cljs-repl :repl-env (weasel/repl-env :ip "0.0.0.0" :port 9001)))
