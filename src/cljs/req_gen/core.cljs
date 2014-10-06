(ns req-gen.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [close! <! >! put! chan]]
            [req-gen.manifest :refer [manifest-json requirements-json]]
            [req-gen.schemas :refer [App]]
            [req-gen.input :refer [nested]]
            [req-gen.utils :refer [p]]
            [om.core :as om :include-macros true]
            [figwheel.client :as figwheel :include-macros true]
            [clojure.browser.repl :as repl]
            [schema.core :as s :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]))

(defonce app-state
  (atom {:manifest {:default-locale "de"
                    :framework-version "1.0"
                    :location "nav_bar"
                    :author {:name "Alistair Roche"
                             :email "a@r.com"}
                    :private true
                    :no-template true}
         :requirements [{:identifier "hello"
                         :type "email_target"
                         :title "Hello"
                         :email "r@a.com"
                         :subject "This is an email"}]}))

(defonce re-render-ch (chan))
(enable-console-print!)
(figwheel/watch-and-reload
 :websocket-url "ws://localhost:3449/figwheel-ws"
 :jsload-callback (fn []
                    (put! re-render-ch true)
                    (print "reloaded")))


(defcomponent root [app owner]
  (will-mount [_]
    (om/set-state! owner :rerender-loop
      (go (loop []
        (when (<! re-render-ch)
          (om/refresh! owner)
          (p "Refreshing Om")
          (recur))))))
  (will-unmount [_]
    (close! (om/get-state owner :rerender-loop)))
  (render [_]
    (dom/div
      (om/build nested app {:opts {:schema App}})
      (om/build manifest-json (:manifest app))
      (om/build requirements-json (:requirements app))
      )))

(om/root
  root
  app-state
  {:target (. js/document (getElementById "app"))})

