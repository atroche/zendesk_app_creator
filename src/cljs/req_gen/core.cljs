(ns req-gen.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [req-gen.dev :refer [is-dev?]]
            [cljs.core.async :refer [<! >! put! chan]]
            [om.core :as om :include-macros true]
            [schema.core :as s :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]))


(def Manifest
  {:default-locale (s/enum "en" "de" "jp")
   :framework-version (s/enum "0.5" "1.0")
   :location (s/enum "nav_bar" "top_bar")
   :author {:name s/Str
            :email s/Str}
   :private s/Bool
   :no-template s/Bool})


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

(defn schema-to-input-component [schema]
  (if (= s/Bool schema)
    checkbox
    text-box))

(defcomponent manifest [app owner]
  (render [_]
    (let [app-json (.stringify js/JSON (clj->js app) nil 2)]
      (dom/pre app-json))))

(defcomponent text-box [app owner {param :param}]
  (render-state [_ {form-chan :form-chan}]
    (dom/input {:type "text"
                :name param
                :value (param app)
                :on-change (fn [event]
                            (put! form-chan [:change param (.-value (.-target event))]))})))

(defn p [msg] (.log js/console msg))

(defn value-from-event [event] (.-value (.-target event)))

(defcomponent checkbox [app owner {param :param}]
  (render-state [_ {form-chan :form-chan}]
    (dom/input {:type "checkbox"
                :name param
                :checked (param app)
                :on-change (fn [event]
                             (let [checked (.. event -target -checked)]
                               (put! form-chan [:change param checked])))})))

(defcomponent manifest-form [app owner]
  (init-state [_]
    {:form-chan (chan)})
  (will-mount [_]
    (let [form-chan (om/get-state owner :form-chan)]
      (go (while true
        (let  [[event-type param new-value] (<! form-chan)]
          (om/update! app param new-value))))))
  (render-state [_ {form-chan :form-chan :as state}]
    (let [param :default-locale]
      (dom/form
        (for [param (keys app)]
          (dom/div
            (dom/label {:for param
                        :class "manifest-label"
                        :style {:display "block"
                                :margin-bottom "4px"
                                :margin-top "14px"}}
                       (str (name param) ":"))
            (om/build (schema-to-input-component (param Manifest))
                      app
                      {:state {:form-chan form-chan}
                               :opts {:param param}})))))))

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

