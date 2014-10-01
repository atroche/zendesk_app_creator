(ns req-gen.schemas
  (:require [schema.core :as s :include-macros true]))

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
