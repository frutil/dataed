(ns frutil.dataed.browser.modules.editor
  (:require
   [frutil.db.tx :as tx]
   [frutil.db.query :as query]

   [frutil.spa.mui :as mui]
   [frutil.spa.mui-form :as mui-form]
   [frutil.spa.mui.item-selector :as item-selector]

   [frutil.dataed.browser.modules :as modules]
   [frutil.dataed.browser.core :as browser]))


;;; init-db

(defn init-db [db]
  db)


;;; commons

(defn edit-supported-for-type? [v]
  (or
   (string? v)))

(defn edit-supported-for-attribute? [attr-entity]
  (and
   (not= (get attr-entity :db/valueType) :db.type/ref)))



;;; command: add-fact

(defn add-fact-veto [{:keys [db e a v]}]
  (or
   (when-not e "no entity")
   (when (and a (query/attribute-is-ref? db a)) "attribute is a reference")
   (when (and a (not (query/attribute-is-many? db a))) "attribute is not arity many")
   (when v "value selected")))

(defn add-fact-to-attribute [{:keys [e a transact]}]
  (mui-form/show-form-dialog
   {:on-submit (fn [form]
                 (let [v (get-in form [:fields a :value])]
                   (transact :add-fact e a v)))}
   {:id a
    :field-type :text
    :required? true
    :label (str a)}))

(defn add-fact [{:keys [db a] :as context}]
  (if a
    (add-fact-to-attribute context)
    (mui/show-dialog
     [item-selector/Dialog
      {:on-item-selected (fn [{:keys [ident]}]
                           (add-fact-to-attribute (assoc context :a ident)))
       :items (map (fn [a]
                     {:ident a})
                   (query/attributes-in-schema
                    db edit-supported-for-attribute?))}])))

(def add-fact-command
  {:ident :add-fact
   :text "add fact"
   :f add-fact
   :veto-f add-fact-veto})


;;; command: add-component

(defn add-component-veto [{:keys [db e a v]}]
  (or
   (when-not e "no entity")
   (when-not (and a (query/attribute-is-component? db a)) "attribute is not component")
   (when (and a (not (query/attribute-is-many? db a))) "attribute is not arity many")
   (when v "value selected")))


(defn add-component [{:keys [db e a transact] :as context}]
  (if a
    (transact :add-component {} e a)
    (mui/show-dialog
     [item-selector/Dialog
      {:on-item-selected (fn [{:keys [ident]}]
                           (transact :add-component {} e ident))
       :items (map (fn [a]
                     {:ident a})
                   (query/attributes-in-schema
                    db edit-supported-for-attribute?))}])))

(def add-component-command
  {:ident :add-component
   :text "add component"
   :f add-component
   :veto-f add-component-veto})

;;; command: retract-fact

(defn retract-fact-veto [{:keys [e a v]}]
  (or
   (when-not e "no entity")
   (when-not a "no attribute")
   (when-not v "value")
   (when (query/attribute-is-reverse-ref? a) "reverse reference")))

(defn retract-fact [{:keys [e a v transact]}]
  (transact :retract-fact e a v))

(def retract-fact-command
  {:ident :retract-fact
   :text "retract fact"
   :f retract-fact
   :veto-f retract-fact-veto})


;;; command: retract-entity

(defn retract-entity-veto [{:keys [e a v]}]
  (or
   (when-not e "no entity")
   (when a "attribute")
   (when v "value")))

(defn retract-entity [{:keys [e transact]}]
  (transact :retract-entity e))

(def retract-entity-command
  {:ident :retract-entity
   :text "retract entity"
   :f retract-entity
   :veto-f retract-entity-veto})


;;; command: edit-fact

(defn edit-fact-veto [{:keys [e a v]}]
  (or
   (when-not e "no entity")
   (when-not a "no attribute")
   (when-not v "value")
   (when (query/attribute-is-reverse-ref? a) "reverse reference")
   (when-not (edit-supported-for-type? v) "type not supported")))


(defn edit-fact [{:keys [e a v transact]}]
  (mui-form/show-form-dialog
   {:on-submit (fn [form]
                 (let [new-v (get-in form [:fields a :value])]
                   (when-not (= new-v v)
                     (transact :update-fact e a v new-v))))}
   {:id a
    :field-type :text
    :value v
    :required? true
    :label (str a)}))

(def edit-fact-command
  {:ident :edit-fact
   :text "edit fact"
   :f edit-fact
   :veto-f edit-fact-veto})


;;; module definition

(defn module []
  {:ident :editor
   :init-db-f init-db
   :schema []
   :commands [edit-fact-command
              retract-fact-command
              retract-entity-command
              add-fact-command]})
              ;add-component-command]})


(modules/reg-module! (module))
