(ns frutil.dataed.modules.browser
  (:require
   [frutil.db.tx :as tx]))


;;; init-db

(defn init-db [db]
  db)

;;; command: goto

(defn goto [])

(def goto-command
  {:ident :goto
   :text "go to selected entity"
   :f goto})


;;; module definition

(defn module []
  {:ident :browser
   :init-db-f init-db
   :schema []
   :commands [goto-command]})
