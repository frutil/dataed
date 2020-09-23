(ns frutil.dataed.browser.modules.browser
  (:require
   [frutil.db.tx :as tx]
   [frutil.db.query :as query]
   [frutil.dataed.browser.modules :as modules]

   [frutil.dataed.browser.core :as browser]))


;;; init-db

(defn init-db [db]
  db)


;;; command goto-attribute-schema

(defn goto-attribute-schema-veto [{:keys [a]}]
  (or
   (when-not a "no attribute selected")))

(defn goto-attribute-schema [{:keys [db a fail!]}]
  (let [e (query/e-wheres db ['?e :db/ident a])]
    (when-not e (fail! (str "Attribute schema definition does not exist.")))
    (browser/goto-entity e)))

(def goto-attribute-schema-command
  {:ident :goto-attribute-schema
   :text "go to attribute schema"
   :f goto-attribute-schema
   :veto-f goto-attribute-schema-veto})


;;; command: goto-entity

(defn goto-entity-veto [{:keys [db a]}]
  (or
   (when-not a "no reference selected")
   (when-not (query/attribute-is-ref? db a)
     (str "not a reference attribute: " a))))


(defn goto-entity [{:keys [v]}]
  (browser/goto-entity v))


(def goto-entity-command
  {:ident :goto-entity
   :text "go to selected entity"
   :f goto-entity
   :veto-f goto-entity-veto})


;;; module definition

(defn module []
  {:ident :browser
   :init-db-f init-db
   :schema []
   :commands [goto-entity-command
              goto-attribute-schema-command]})


(modules/reg-module! (module))
