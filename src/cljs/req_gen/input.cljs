(ns req-gen.input
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! >! put! chan]]
            [req-gen.utils :refer [p pclj]]
            [req-gen.components.fields :refer [checkbox select text-box label]]
            [req-gen.schemas :refer [Requirements empty-state-from-schema
                                     get-add-fn get-base-schema]]
            [om.core :as om :include-macros true]
            [schema.core :as s :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]))

(declare nested list-of-fields)

(defn schema-to-input-component [schema]
  (cond
    (= s/Bool schema) checkbox
    (= s/EnumSchema (type schema)) select
    ((set (vals Requirements)) schema) list-of-fields
    (map? (s/explain schema)) nested
    (vector? (s/explain schema)) list-of-fields
    :else text-box))

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
        (dom/div
        (label field-name)
        (let [field-schema (field-name schema)
              component (schema-to-input-component field-schema)]
          (om/build component
                    (if (#{text-box checkbox select} component) fields field-value)
                    {:state {:form-chan fields-chan}
                     :opts {:param field-name
                            :schema field-schema}})))))))

(defcomponent list-of-fields [fields owner {schema :schema}]
  (init-state [_]
    {:add-chan (chan)})
  (will-mount [_]
    (let [add-chan (om/get-state owner :add-chan)]
      (go (while (<! add-chan)
        (om/transact! fields
                      (get-add-fn schema))))))
  (render-state [_ {add-chan :add-chan}]
    (dom/div
      (dom/button {:class "btn btn-primary"
                   :on-click (fn [e] (put! add-chan [:add]))}
                  "Add Field")
      (let [fields (if (vector? schema)
                     (map-indexed vector fields)
                     fields)]
        (for [[identifier field] fields]
          (dom/div
          (om/build nested
                    field
                    {:opts {:schema (get-base-schema schema)}})))))))

