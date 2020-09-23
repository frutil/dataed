(ns frutil.dataed.browser.modules.annotations
  (:require
   [frutil.db.tx :as tx]
   [frutil.dataed.browser.modules :as modules]))

(defn init [db]
  db)


(defn add-annotation [{:keys [e transact]}]
  (transact
   (fn [db]
     (tx/db-with-component
      db
      {:annotations.annotation/text "new annotation"}
      [[e :annotations.container/annotations]]))))


(defn module []
  {:ident :annotations
   :init-f init
   :schema [{:db/ident       :annotations.container/annotations
             :db/valueType   :db.type/ref
             :db/isComponent true
             :db/cardinality :db.cardinality/many}

            {:db/ident       :annotations.annotation/text
             :db/valueType   :db.type/string}]
   :commands [{:ident ::add-annotation
               :text "Add Annotation"
               :f add-annotation}]})


(modules/reg-module! (module))
