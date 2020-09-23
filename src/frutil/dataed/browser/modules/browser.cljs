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

(defn goto-attribute-schema [{:keys [db a fail!]}]
  (let [e (query/e-wheres db ['?e :db/ident a])]
    (when-not e (fail! (str "Attribute schema definition does not exist.")))
    (browser/goto-entity e)))

(def goto-attribute-schema-command
  {:ident :goto-attribute-schema
   :text "go to attribute schema"
   :f goto-attribute-schema})


;;; command: goto-entity

(defn goto-entity [{:keys [v]}]
  (browser/goto-entity v))


(def goto-entity-command
  {:ident :goto-entity
   :text "go to selected entity"
   :f goto-entity})


;;; module definition

(defn module []
  {:ident :browser
   :init-db-f init-db
   :schema []
   :commands [goto-entity-command
              goto-attribute-schema-command]})


(modules/reg-module! (module))
