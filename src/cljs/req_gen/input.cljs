(ns req-gen.input
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! >! put! chan]]
            [req-gen.utils :refer [p pclj]]
            [req-gen.schemas :refer [Requirements empty-state-from-schema]]
            [om.core :as om :include-macros true]
            [schema.core :as s :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]))

(declare nested list-of-fields checkbox select text-box requirements)

(defn schema-to-input-component [schema]
  (cond
    (= s/Bool schema) checkbox
    (= s/EnumSchema (type schema)) select
    ((set (vals Requirements)) schema) requirements
    (map? (s/explain schema)) nested
    (vector? (s/explain schema)) list-of-fields
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



(defcomponent select [app owner {param :param
                                 schema :schema}]
  (render-state [_ {form-chan :form-chan}]
    (dom/select {:name param
                 :value (param app)
                 :on-change (fn [event]
                              (let [value (.. event -target -value)]
                                (put! form-chan [:change param value])))}
      (for [option (rest (s/explain schema))]
        (dom/option {:value option} option)))))

(defn label [for-field]
  (dom/label {:for for-field
              :class "field-label"
              :style {:display "block"
                      :margin-bottom "4px"
                      :margin-top "14px"}}
             (str (name for-field) ":")))


(defcomponent nested [fields owner {schema :schema}]
  (init-state [_]
    {:fields-chan (chan)})
  (will-mount [_]
    (let [fields-chan (om/get-state owner :fields-chan)]
      (go (while true
        (let  [[_ field-name new-value] (<! fields-chan)]
          (om/update! fields field-name new-value))))))
  (render-state [_ {fields-chan :fields-chan}]
    (dom/div
      (for [[field-name field-value :as field] fields]
        (do (pclj field-name) (dom/div
          (label field-name)
          (let [field-schema (field-name schema)
                component (schema-to-input-component field-schema)]
            (pclj field-schema)
            (om/build component
                      (if (#{text-box checkbox select} component) fields field-value)
                      {:state {:form-chan fields-chan}
                       :opts {:param field-name
                              :schema field-schema}}))))))))

(defn add-new-instance [schema old-ones]
  (let [new-name (str "random_id_" (rand))
        new-one {new-name (empty-state-from-schema schema)}]
    (merge old-ones new-one)))

(defn add-new-instance-to-list [schema old-ones]
  (let [new-one (empty-state-from-schema schema)]
    (conj old-ones new-one)))

(defcomponent requirements [reqs owner {schema :schema}]
  (init-state [_]
    {:add-chan (chan)})
  (will-mount [_]
    (let [add-chan (om/get-state owner :add-chan)]
      (go (while (<! add-chan)
        (om/transact! reqs
                      (partial add-new-instance (schema s/Keyword)))))))
  (render-state [_ {add-chan :add-chan}]
    (dom/div
      (dom/button {:class "btn btn-primary"
                   :on-click (fn [e] (put! add-chan [:add]))}
                  "Add Requirement")
      (for [[identifier requirement-data] reqs]
        (do
          (dom/div
          (label identifier)
          (om/build nested
                    requirement-data
                    {:opts {:schema (schema s/Keyword)}})))))))

(defcomponent list-of-fields [fields owner {schema :schema}]
  (init-state [_]
    {:add-chan (chan)})
  (will-mount [_]
    (let [add-chan (om/get-state owner :add-chan)]
      (go (while (<! add-chan)
        (om/transact! fields
                      (partial add-new-instance-to-list (first schema)))))))
  (render-state [_ {add-chan :add-chan}]
    (dom/div
      (dom/button {:class "btn btn-primary"
                   :on-click (fn [e] (put! add-chan [:add]))}
                  "Add Field")
      (for [field fields]
        (do
          (dom/div
          (om/build nested
                    field
                    {:opts {:schema (first schema)}})))))))

