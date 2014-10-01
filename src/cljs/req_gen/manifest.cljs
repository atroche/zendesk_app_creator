(ns req-gen.manifest
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [req-gen.dev :refer [is-dev?]]
            [req-gen.input :refer [schema-to-input-component]]
            [cljs.core.async :refer [<! >! put! chan]]
            [om.core :as om :include-macros true]
            [schema.core :as s :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]))

(def DefaultLocale (s/enum "en" "de" "jp"))
(def FrameworkVersion (s/enum "0.5" "1.0"))
(def Location (s/enum "nav_bar" "top_bar"))

(def Manifest
  {:default-locale DefaultLocale
   :framework-version FrameworkVersion
   :location Location
   :author {:name s/Str
            :email s/Str}
   :private s/Bool
   :no-template s/Bool})

(defn pretty-json [data]
  (.stringify js/JSON (clj->js data) nil 2))

(defcomponent manifest [app owner]
  (render [_]
    (dom/pre (pretty-json app))))

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
