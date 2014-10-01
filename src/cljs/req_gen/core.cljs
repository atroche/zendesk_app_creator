(ns req-gen.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [close! <! >! put! chan]]
            [req-gen.manifest :refer [manifest manifest-form]]
            [req-gen.helpers :refer [p]]
            [om.core :as om :include-macros true]
            [figwheel.client :as figwheel :include-macros true]
            [clojure.browser.repl :as repl]
            [schema.core :as s :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]))

(defonce re-render-ch (chan))


(enable-console-print!)

(figwheel/watch-and-reload
 :websocket-url "ws://localhost:3449/figwheel-ws"
 :jsload-callback (fn []
                    (put! re-render-ch true)
                    (print "reloaded")))

(defonce app-state
  (atom {:app {:default-locale "en"
               :framework-version "1.0"
               :location "nav_bar"
               :author {:name "Alistair Roche"
                        :email "roche.a@gmail.com"}
               :private true
               :no-template true}
         :requirements {:targets {:a_basecamp_target {:title "A sample target"
                                                      :type "basecamp_target"
                                                      :active true
                                                      :target_url "http://mytarget.com"
                                                      :token "123456"
                                                      :project_id "9999"
                                                      :resource "todo"}}}}))


(defcomponent root [app owner]
  (will-mount [_]
    (om/set-state! owner :rerender-loop
      (go (loop []
        (when (<! re-render-ch)
          (om/refresh! owner)
          (recur))))))
  (will-unmount [_]
    (close! (om/get-state owner :rerender-loop)))
  (render [_]
    (dom/div
      (om/build manifest-form (:app app))
      (om/build manifest (:app app)))))

(om/root
  root
  app-state
  {:target (. js/document (getElementById "app"))})

