(ns frutil.dataed.browser.core
  (:require

   [datascript.core :as d]

   ;; frutil
   [frutil.db.datascript :as datascript]
   [frutil.db.tx :as tx]

   ;; spa
   [frutil.spa.state :as state]
   [frutil.spa.navigation :as navigation]
   [frutil.spa.mui :as mui]

   ;; dataed
   [frutil.dataed.browser.modules :as modules]))


(def modules modules/modules!)

(defn db-name [] (navigation/param :db-name))
(defn root-e [] (navigation/param :root-e))


(state/def-state dbs {:ident :dbs
                      :localstorage? true})
                      ;:localstorage-save-transform-f db/serializable-value})
(def db (state/new-navigation-param-pointer dbs :db-name))


(state/def-state cursors {:localstorage? true})
(def cursor (state/new-navigation-param-pointer cursors :db-name))


(defn goto-entity [id]
  (js/console.log "goto-entity" id)
  (navigation/push-state :browser {:db-name (db-name)
                                   :root-e id}))

(def tx-map
  {:add-component tx/add-component
   :update-fact tx/update-fact
   :retract-fact tx/retract-fact})


(defn transactor [db-name]
  (fn [tx-id & args]
    (js/console.log "TRANSACTION" tx-id args)
    (let [tx-f (get tx-map tx-id)
          _ (when-not tx-f (throw (ex-info (str "unsupported tx: " tx-id)
                                           {:tx-id tx-id
                                            :args args})))
          tx-data (apply tx-f (into [(db db-name)] args))]
      (state/update! dbs db-name
                     #(d/db-with % tx-data)))))


(defn command-failer [command]
  (fn [message]
    (throw (ex-info message {::command-aborter-message message
                             :command command}))))


(defn- command-veto [command context]
  (try
    (when-let [f (get command :veto-f)]
      (f context))
    (catch :default ex
      (js/console.log "Exception in command veto-f for command" command "\n" ex)
      ex)))


(defn execute-command [module-ident command-ident context]
  ;; (js/console.log "EXECUTE COMMAND" command-ident)
  (try
    (let [command (modules/command (modules) module-ident command-ident)
          context (assoc context
                         :db (db)
                         :transact (transactor (db-name))
                         :fail! (command-failer command))
          f (get command :f)]
      (f context))
    (catch :default ex
      (js/console.log "EXCEPTION" ex)
      (if-let [message (get (ex-data ex) ::command-aborter-message)]
        (mui/show-error-dialog message)
        (mui/show-exception-dialog ex)))))


(defn command-selector-options [e a c v]
  (let [context {:e e :a a :c c :v v :db (db)}]
    {:items (for [module (modules)
                  command (modules/module-commands (modules) (get module :ident))
                  :when (nil? (command-veto command context))]
              {:command command
               :module module
               :ident (-> command :ident)
               :primary (or (-> command :text)
                            (-> command :ident str)
                            "<unidentifiable command>")
               :secondary (-> command :description)})
     :on-item-selected (fn [item]
                         (execute-command (get-in item [:module :ident])
                                          (get-in item [:command :ident])
                                          {:e e :a a :c c :v v :db (db)}))}))


(defn create-db [db-name]
  (js/console.log "CREATE EMPTY DB" db-name)
  (let [k db-name
        schema (modules/schema (modules))
        db (datascript/new-db-with-schema schema)]
    (state/set! dbs k db nil)
    (state/set! cursors k 0 nil)))
