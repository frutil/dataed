(ns frutil.dataed.browser
  (:require

   [reagent-material-ui.core.card :refer [card]]
   [reagent-material-ui.core.card-content :refer [card-content]]
   [reagent-material-ui.core.card-action-area :refer [card-action-area]]
   [reagent-material-ui.core.toolbar :refer [toolbar]]
   [reagent-material-ui.core.paper :refer [paper]]
   [reagent-material-ui.core.button :refer [button]]
   [reagent-material-ui.core.icon-button :refer [icon-button]]
   [reagent-material-ui.core.menu :refer [menu]]
   [reagent-material-ui.core.menu-item :refer [menu-item]]

   [reagent-material-ui.icons.more-vert :refer [more-vert]]

   [frutil.devtools :as dev]
   [frutil.db.browser :as b]

   [frutil.spa.state :as state]
   [frutil.spa.mui :as mui]
   [frutil.spa.mui.item-selector :as item-selector]

   [frutil.db.core :as db]
   [frutil.db.commands :as commands]
   [frutil.db.modules.annotations :as annotations]
   [frutil.db.modules.brainstorming :as brainstorming]
   [clojure.string :as str]))


(state/def-state db {})
(state/def-state root {})
(state/def-state cursor {})


(defn initialize-with-dummy! []
  (let [dummy-db (-> (db/new-db {:modules [(annotations/module)
                                           (brainstorming/module)]}))
        root-id (db/root-id dummy-db)]
    (state/set! db :singleton dummy-db nil)
    (state/set! root :singleton root-id nil)
    (state/set! cursor :singleton root-id nil)))

(initialize-with-dummy!)


;;; commands


(defn entity-item-selector-options [e]
  {:items (map (fn [command]
                 {:command command
                  :ident (-> command :ident)
                  :primary (or (-> command :text)
                               (-> command :ident str)
                               "hello")
                  :secondary (or (-> command :description))})
               (commands/commands (db) e))
   :on-item-selected (fn [{:keys [command]}]
                       (state/update!
                        db :singleton
                        commands/execute e command))})




;;; entities

(defn Value [v]
  (mui/Data v))


(declare Entity)


(defn Ref [e]
  ;; [mui/Card
  ;;  [:i "#" (get entity :db/id)]])
  [Entity e])


(defn EntityAttributeValue [a v]
  (if (db/attribute-is-ref? (db) a)
    ^{:key v} [Ref v]
    ^{:key v} [Value v]))


(defn EntityAttribute [a v]
  [:div
   [:div [:b (str a)]]
   (if (db/attribute-is-many? (db) a)
     [:div.flex
      (for [v v]
        ^{:key v}
        [EntityAttributeValue a v])]
     [EntityAttributeValue a v])])


(defn Entity [e]
  (let [entity (db/entity (db) e)
        as (-> entity keys sort)]
    [card
     [:div.flex
      {:style {:justify-content :space-between
               :align-items :center
               :padding "0.5rem 0.5rem 0 1rem"}}
      [:div
       [:i "#" e]]
      [icon-button
       {:on-click #(mui/show-dialog [item-selector/Dialog
                                     (entity-item-selector-options e)])
        :size :small}
       [more-vert]]]
     [card-content
      [:div.stack
       (for [a as]
         ^{:key a}
         [EntityAttribute a (get entity a)])]]]))


(defn EntitiesList [es]
  [:div.EntitiesList.flex
   (for [e es]
     ^{:key e}
     [Entity e])])


(defn Statusbar []
  [:div.Statusbar
   [toolbar
    {:style {:gap "1rem"}}
    [:div
     {:style {:font-weight 900
              :letter-spacing "1px"}}
     "Datäd"]
    [:div "r: " (root)]
    [:div "c: " (cursor)]
    [:div
     "[ "
     (->> (db)
          :modules
          (map :ident)
          (map name)
          sort
          (str/join " "))
     " ]"]]])


(defn Browser []
  [:div
   (when-let [db (db)]
     [:div.stack
      [EntitiesList [(root)]]])])


      ;; (let [db (b/db browser)]
      ;;   [:div
      ;;    [:br][:br][:hr]
      ;;    [mui/Data (-> browser
      ;;                  (b/q
      ;;                   '[:find ?e ?a ?v
      ;;                     :where
      ;;                     [?e ?a ?v]])
      ;;                  (->> (reduce (fn [ret [e a v]]
      ;;                                 (-> ret
      ;;                                     (assoc-in [e a] v)
      ;;                                     (assoc-in [e :db/id] e)))
      ;;                               {})
      ;;                       vals
      ;;                       (sort-by :db/id)))]
      ;;    [:hr]
      ;;    [mui/Data browser]])])])
