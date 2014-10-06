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
(def TicketFieldType (s/enum "text"))

(def Target
  {:type TargetType
   :title s/Str
   :email s/Str
   :subject s/Str})

(def TicketField
  {:type TicketFieldType
   :title s/Str})

(def Condition
  {:field s/Str
   :operator s/Str
   :value s/Str})

(def Action
  {:field s/Str
   :value s/Str})

(def Trigger
  {:title s/Str
   :conditions {:all [Condition]
                :any [Condition]}
   :actions [Action]})

(def TargetMap {s/Keyword Target})
(def TicketFieldMap {s/Keyword TicketField})
(def TriggerMap {s/Keyword Trigger})

(def Requirements
  {:targets TargetMap
   :ticket-fields TicketFieldMap
   :triggers TriggerMap})

(def App
  {:manifest Manifest
   :requirements Requirements})


(defn schema? [value]
  (satisfies? s/Schema value))

(defn empty-state-from-schema [schema]
  (cond
    (not (schema? schema)) schema
    (map? (s/explain schema)) (into {}
                                    (for [[k v] (filter (fn [[k v]] (keyword? k)) schema)]
                                      [k (empty-state-from-schema v)]))
    (vector? (s/explain schema)) []
    (= s/EnumSchema (type schema)) (let [first-enum-value (first (rest (s/explain schema)))]
                                     (empty-state-from-schema first-enum-value))
    :else nil))

