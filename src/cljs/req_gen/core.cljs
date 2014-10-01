(ns req-gen.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [req-gen.dev :refer [is-dev?]]
            [cljs.core.async :refer [<! >! put! chan]]
            [om.core :as om :include-macros true]
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


(defcomponent manifest [app owner]
  (render [_]
    (let [app-json (.stringify js/JSON (clj->js app) nil 2)]
      (dom/pre app-json))))

(defcomponent manifest-input [app owner {param :param}]
  (render-state [_ {form-events-chan :form-events-chan}]
    (dom/input {:type "text"
                :value (param app)
                :onChange (fn [event]
                            (put! form-events-chan [:change param (.-target event)]))})))

(defcomponent manifest-form [app owner]
  (init-state [_]
    {:form-events-chan (chan)})
  (will-mount [_]
    (let [form-events-chan (om/get-state owner :form-events-chan)]
      (go (while true
        (let  [[event-type param target :as blah] (<! form-events-chan)]
          (om/transact! app param #(.-value target)))))))
  (render-state [_ {form-events-chan :form-events-chan :as state}]
    (let [param :default-locale]
      (dom/form
        (om/build manifest-input app {:state {:form-events-chan form-events-chan}
                                      :opts {:param :default-locale}})))))

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

