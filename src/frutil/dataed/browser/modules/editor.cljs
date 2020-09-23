(ns frutil.dataed.browser.modules.editor
  (:require
   [frutil.db.tx :as tx]
   [frutil.db.query :as query]

   [frutil.spa.mui :as mui]
   [frutil.spa.mui-form :as mui-form]

   [frutil.dataed.browser.modules :as modules]


   [frutil.dataed.browser.core :as browser]))


;;; init-db

(defn init-db [db]
  db)


;;; command: edit-value

(defn edit-supported-for-type? [v]
  (or
   (string? v)))


(defn edit-value-veto [{:keys [db e a v]}]
  (or
   (when-not e "no entity")
   (when-not a "no attribute")
   (when-not v "value")
   (when-not (edit-supported-for-type? v) "type not supported")))


(defn transact-value [e a c old-v new-v transact]
  (when-not (= old-v new-v)
    (transact
     (fn [db]
       (tx/db-with db [[:db/retract e a old-v]
                       [:db/add e a new-v]])))))


(defn edit-value [{:keys [e a c v transact]}]
  (mui-form/show-form-dialog
   {:on-submit (fn [form]
                 (transact-value e a c v
                                 (get-in form [:fields a :value])
                                 transact))}
   {:id a
    :field-type :text
    :value v
    :required? true
    :label (str a)
    :auto-focus? true}))


(def edit-value-command
  {:ident :edit-value
   :text "edit value"
   :f edit-value
   :veto-f edit-value-veto})


;;; module definition

(defn module []
  {:ident :editor
   :init-db-f init-db
   :schema []
   :commands [edit-value-command]})



(modules/reg-module! (module))
