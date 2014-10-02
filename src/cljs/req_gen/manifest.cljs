(ns req-gen.manifest
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [req-gen.input :refer [nested]]
            [req-gen.schemas :refer [App Manifest DefaultLocale FrameworkVersion Location Requirements]]
            [req-gen.utils :refer [keys-to-camel-case pclj]]
            [cljs.core.async :refer [<! >! put! chan]]
            [om.core :as om :include-macros true]
            [schema.core :as s :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]))



(defn pretty-json [data]
  (.stringify js/JSON (clj->js data) nil 2))


(defcomponent manifest [app owner]
  (render [_]
    (dom/pre (pretty-json (keys-to-camel-case app)))))

(defcomponent manifest-form [app owner]
  (init-state [_]
    {:form-chan (chan)})
  (will-mount [_]
    (let [form-chan (om/get-state owner :form-chan)]
      (go (while true
        (let [[event-type param new-value] (<! form-chan)]
          (om/update! app param new-value))))))
  (render-state [_ {form-chan :form-chan :as state}]
    (dom/form
      (om/build nested app {:opts {:schema App}}))))


