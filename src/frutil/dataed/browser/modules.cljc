(ns frutil.dataed.browser.modules)


(defonce REGISTRY (atom {}))


(defn reg-module [module]
  (swap! REGISTRY assoc (get module :ident) module))


(defn modules []
  (-> @REGISTRY vals))


(defn commands [modules]
  (reduce (fn [ret module]
            (concat ret (get module :commands)))
          [] modules))


(defn schema [modules]
  (reduce (fn [schema module]
            (concat schema (get module :schema)))
          '() modules))


(defn execute-command [e command transact-f]
  (let [f (or (get command :f) identity)]
    (f e transact-f)))
