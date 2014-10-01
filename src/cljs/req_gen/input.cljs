(ns req-gen.input
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! >! put! chan]]
            [req-gen.utils :refer [p pclj]]
            [req-gen.schemas :refer [Manifest Author]]
            [om.core :as om :include-macros true]
            [schema.core :as s :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]))

(declare checkbox select text-box)


(defn schema-to-input-component [schema]
  (cond
    (= s/Bool schema) checkbox
    (= s/EnumSchema (type schema)) select
    (= Author schema) author-info
    :else text-box))

(defcomponent text-box [app owner {param :param}]
  (render-state [_ {form-chan :form-chan}]
    (dom/input {:type "text"
                :name param
                :value (get app param)
                :on-change (fn [event]
                            (put! form-chan [:change param (.-value (.-target event))]))})))

(defcomponent checkbox [app owner {param :param}]
  (render-state [_ {form-chan :form-chan}]
    (dom/input {:type "checkbox"
                :name param
                :checked (param app)
                :on-change (fn [event]
                             (let [checked (.. event -target -checked)]
                               (put! form-chan [:change param checked])))})))



(defcomponent select [app owner {param :param}]
  (render-state [_ {form-chan :form-chan}]
    (dom/select {:name param
                 :value (param app)
                 :on-change (fn [event]
                              (let [value (.. event -target -value)]
                                (put! form-chan [:change param value])))}
      (for [option (rest (s/explain (param Manifest)))]
        (dom/option {:value option} option)))))


(defcomponent author-info [app owner {param :param}]
  (render-state [_ {form-chan :form-chan}]
    (pclj (param app))
    (dom/div
    (for [[inner-param inner-schema] (param app)]
    (do
      (dom/div
        (dom/label {:for param
                    :class "manifest-label"
                    :style {:display "block"
                            :margin-bottom "4px"
                            :margin-top "14px"}}
                   (str (name inner-param) ":"))
        (let [component (schema-to-input-component inner-schema)]
          (om/build component app {:state {:form-chan form-chan}
                                   :opts {:param [param inner-param]}}))))))))
