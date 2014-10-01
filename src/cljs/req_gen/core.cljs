(ns req-gen.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [req-gen.dev :refer [is-dev?]]
            [cljs.core.async :refer [<! >! put! chan]]
            [req-gen.manifest :refer [manifest manifest-form]]
            [req-gen.helpers :refer [p]]
            [om.core :as om :include-macros true]
            [schema.core :as s :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]))

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


(om/root
  (fn [app owner]
    (reify om/IRender
      (render [_]
        (dom/h1 "App Builder")
        (dom/div
          (om/build manifest-form (:app app))
          (om/build manifest (:app app))))))
  app-state
  {:target (. js/document (getElementById "app"))})

