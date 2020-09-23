(ns frutil.dataed.browser.core
  (:require

   ;; frutil
   [frutil.db.datascript :as datascript]

   ;; spa
   [frutil.spa.state :as state]
   [frutil.spa.navigation :as navigation]

   ;; dataed
   [frutil.dataed.browser.modules :as modules]))


(def modules modules/modules)

(defn db-name [] (navigation/param :db-name))
(defn root-e [] (navigation/param :root-e))


(state/def-state dbs {:ident :dbs
                      :localstorage? true})
                      ;:localstorage-save-transform-f db/serializable-value})
(def db (state/new-navigation-param-pointer dbs :db-name))


(state/def-state cursors {:localstorage? true})
(def cursor (state/new-navigation-param-pointer cursors :db-name))


(defn goto-entity [id]
  (navigation/push-state :browser {:db-name (db-name)
                                   :root-e id}))


(defn transactor [db-name]
  (fn [transact]
    (state/update! dbs db-name transact)))


(defn entity-item-selector-options [e a c v]
  {:items (map (fn [command]
                 {:command command
                  :ident (-> command :ident)
                  :primary (or (-> command :text)
                               (-> command :ident str)
                               "<unidentifiable command>")
                  :secondary (or (-> command :description))})
               (modules/commands (modules)))
   :on-item-selected (fn [{:keys [command]}]
                       (modules/execute-command e command (transactor (db-name))))})


(defn create-db [db-name]
  (js/console.log "CREATE EMPTY DB" db-name)
  (let [k db-name
        schema (modules/schema (modules))
        db (datascript/new-db-with-schema schema)]
    (state/set! dbs k db nil)
    (state/set! cursors k 0 nil)))
