(ns req-gen.schemas
  (:require [schema.core :as s :include-macros true]))

(def DefaultLocale (s/enum "en" "de" "jp"))
(def FrameworkVersion (s/enum "0.5" "1.0"))
(def Location (s/enum "nav_bar" "top_bar"))
(def Author {:name s/Str
             :email s/Str})

(def Manifest
  {:default-locale DefaultLocale
   :framework-version FrameworkVersion
   :location Location
   :author Author
   :private s/Bool
   :no-template s/Bool})

(def TargetType (s/enum "email_target"))

(def Target
  {:type TargetType
   :title s/Str
   :email s/Str
   :subject s/Str})

(def TargetMap {s/Keyword Target})

(def Requirements
  {:targets TargetMap})

(def App
  {:manifest Manifest
   :requirements Requirements})

(def basecamp-target-reqs {:targets {:a_basecamp_target {:title "A sample target"
                                                :type "basecamp_target"
                                                :active true
                                                :target_url "http://mytarget.com"
                                                :token "123456"
                                                :project_id "9999"
                                                :resource "todo"}}})

(def target-reqs {:targets {:an_email_target {:title "A sample target"
                                             :type "email_target"
                                             :subject "How are you?"
                                             :email "b@a.com"}}})

(defn schema? [value]
  (satisfies? s/Schema value))
(enable-console-print!)

(defn empty-state-from-schema [schema]
  (cond
    (not (schema? schema)) schema
    (map? (s/explain schema)) (into {}
                                    (for [[k v] (filter (fn [[k v]] (keyword? k)) schema)]
                                      [k (empty-state-from-schema v)]))
    (vector? (s/explain schema)) (mapv empty-state-from-schema schema)
    (= s/EnumSchema (type schema)) (let [first-enum-value (first (rest (s/explain schema)))]
                                     (empty-state-from-schema first-enum-value))
    :else nil))

