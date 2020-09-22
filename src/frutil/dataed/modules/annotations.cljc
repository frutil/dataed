(ns frutil.dataed.modules.annotations
  (:require
   [frutil.db.core :as db]))


(defn init [db]
  db)


(defn add-annotation [e transact]
  (transact
   (fn [db]
     (let [db (db/transact
               db [[:db/add -1 :annotations.annotation/text ""]])
           id (db/tempid db -1)
           db (db/transact
               db [[:db/add e :annotations.container/annotations id]])]
       db))))


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


