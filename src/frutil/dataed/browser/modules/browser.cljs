(ns frutil.dataed.browser.modules.browser
  (:require
   [frutil.db.tx :as tx]
   [frutil.dataed.browser.modules :as modules]

   [frutil.dataed.browser.core :as browser]))


;;; init-db

(defn init-db [db]
  db)

;;; command: goto

(defn goto-entity []
  (browser/goto-entity 1))


(def goto-entity-command
  {:ident :goto
   :text "go to selected entity"
   :f goto-entity})


;;; module definition

(defn module []
  {:ident :browser
   :init-db-f init-db
   :schema []
   :commands [goto-entity-command]})


(modules/reg-module (module))
