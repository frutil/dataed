(ns frutil.dataed.modules.software
  (:require
   [frutil.db.tx :as tx]))


(defn init [db]
  db)




(defn module []
  {:ident :software
   :init-f init
   :schema [{:db/ident       :annotations.container/annotations
             :db/valueType   :db.type/ref
             :db/isComponent true
             :db/cardinality :db.cardinality/many}

            {:db/ident       :annotations.annotation/text
             :db/valueType   :db.type/string}]
   :commands (fn [_db _e]
               [{:ident ::add-annotation
                 :text "Add Annotation"}])})
