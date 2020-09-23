(ns frutil.dataed.browser.modules)


(defonce REGISTRY (atom {}))


(defn reg-module! [module]
  (swap! REGISTRY assoc (get module :ident) module))


(defn modules! []
  (-> @REGISTRY vals))


;;;

(defn module [modules module-ident]
  (->> modules
       (filter #(= module-ident (get % :ident)))
       first))


(defn commands [modules]
  (reduce (fn [ret module]
            (concat ret (get module :commands)))
          [] modules))


(defn module-commands [modules module-ident]
  (-> (module modules module-ident) :commands))


(defn command [modules module-ident command-ident]
  (->> (module-commands modules module-ident)
       (filter #(= command-ident (get % :ident)))
       first))


(defn schema [modules]
  (reduce (fn [schema module]
            (concat schema (get module :schema)))
          '() modules))


(defn execute-command [e command transact-f]
  (let [f (or (get command :f) identity)]
    (f e transact-f)))
