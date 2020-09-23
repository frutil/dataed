(ns frutil.dataed.browser.modules.annotations
  (:require
   [frutil.db.tx :as tx]
   [frutil.dataed.browser.modules :as modules]))

(defn init [db]
  db)


;;; command add-annotation

(defn add-annotation [{:keys [e transact]}]
  (transact
   (fn [db]
     (tx/db-with-component
      db
      {:annotations.annotation/text "new annotation"}
      [[e :annotations.container/annotations]]))))

(def add-annotation-command
  {:ident ::add-annotation
   :text "add annotation"
   :description "Adds a new annotation entity to the selected entity."
   :f add-annotation})


;;; module definition


(defn module []
  {:ident :annotations
   :init-f init
   :schema [{:db/ident       :annotations.container/annotations
             :db/valueType   :db.type/ref
             :db/isComponent true
             :db/cardinality :db.cardinality/many}

            {:db/ident       :annotations.annotation/text
             :db/valueType   :db.type/string}]
   :commands [add-annotation-command]})


(modules/reg-module! (module))
