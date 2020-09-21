(ns frutil.dataed.browser
  (:require
   [clojure.string :as str]

   [reagent-material-ui.colors :as colors]

   [reagent-material-ui.core.typography :refer [typography]]
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

   [frutil.spa.navigation :as navigation]
   [frutil.spa.state :as state]
   [frutil.spa.mui :as mui]
   [frutil.spa.mui.item-selector :as item-selector]

   [frutil.db.core :as db]

   [frutil.dataed.commands :as commands]
   [frutil.dataed.modules.annotations]))



(defn db-name [] (navigation/param :db-name))
(state/def-state dbs {:localstorage? true
                      :localstorage-save-transform-f db/serializable-value})
(defn db [] (dbs (db-name)))
(state/def-state roots {:localstorage? true})
(defn root [] (roots (db-name)))
(state/def-state cursors {:localstorage? true})
(defn cursor [] (cursors (db-name)))


(defn transactor [db-name]
  (fn [transact]
    (state/update! dbs db-name transact)))


;; (defn initialize-with-dummy! []

;; (defonce initialized
;;   (do
;;     (initialize-with-dummy!)
;;     true))


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
                       (commands/execute e command (transactor (db-name))))})


;;; commons

(defn ActionCard [{:keys [elevation on-click color]} content]
  [card
   {:elevation elevation
    :style {:overflow :unset
            :background-color color}}
   [card-action-area
    {:on-click on-click
     :class :height-100
     :component :div}
    [card-content
     {:class :sticky}
     content]]])


(defn TreeCardsWrapper [parent & children]
  [:div.TreeCardsWrapper
   {:style {:display :flex
            :gap "8px"
            :align-items :stretch}}
   parent
   [:div.TreeCardsWrapper-Children
    {:style {:display :flex
             :flex-direction :column
             :gap "16px"}}
    children]])


;;; entities


(def palette
  {:entity :white
   :attribute :white
   :value-seq :white
   :value :white})
  ;; {:entity (get colors/grey 50)
  ;;  :attribute (get colors/light-green 100)
  ;;  :value-seq (get colors/indigo 50)
  ;;  :value (str (get colors/indigo 100))})


(defn elevation [entity-path sub-level]
  (+ sub-level (* 3 (count entity-path))))


(defn Value [v entity-path]
  [ActionCard
   {:elevation 3
    :color (-> palette :value)}
   (if (string? v)
     (if (= v "")
       [:span
        "<empty string>"]
       v)
     (mui/Data v))])


(declare Entity)


(defn Ref [e entity-path]
  ;; [mui/Card
  ;;  [:i "#" (get entity :db/id)]])
  [Entity e (conj entity-path :>)])


(defn AttributeValue [a v entity-path]
  (if (db/attribute-is-ref? (db) a)
    ^{:key v} [Ref v entity-path]
    ^{:key v} [Value v entity-path]))


(defn AttributeValueCollection [a vs entity-path]
  [TreeCardsWrapper
   [ActionCard
    {:elevation (elevation entity-path 3)
     :color (-> palette :value-seq)}
    [:div "⁞"]]
   (for [v vs]
     ^{:key v}
     [AttributeValue a v entity-path])])


(defn Attribute [a v entity-path]
  [:div
   {:style {:display :flex
            :gap "8px"}}
   [ActionCard
    {:elevation (elevation entity-path 2)
     :color (-> palette :attribute)}
    [mui/Caption a]]
   (if (db/attribute-is-many? (db) a)
     [AttributeValueCollection a v entity-path]
     [AttributeValue a v entity-path])])




(defn Entity [e entity-path]
  (let [entity (db/entity (db) e)
        as (-> entity keys sort)
        on-entity-clicked #(mui/show-dialog [item-selector/Dialog
                                             (entity-item-selector-options e)])]
    [:div.Entity
     [TreeCardsWrapper
      [ActionCard
       {:elevation (elevation entity-path 1)
        :on-click on-entity-clicked
        :color (-> palette :entity)}
       [:div [:i "#" e]]]
      (for [a as]
        ^{:key a}
        [Attribute a (get entity a) entity-path])]]))


(defn EntitiesList [es]
  [:div.EntitiesList.flex
   (for [e es]
     ^{:key e}
     [Entity e []])])


(defn Statusbar []
  [:div.Statusbar
    [toolbar
     {:style {:gap "1rem"}}
     [:div
      [:img {:src "dataed.svg"
             :height "32px"}]]
     [:div
      {:style {:font-weight 900
               :letter-spacing "1px"}}
      "Dataed"]
     [:div (db-name)]
     (when-let [r (root)] [:div "r: " r])
     (when-let [c (cursor)] [:div "c: " c])
     (when-let [db (db)]
       [:div
        "[ "
        (->> db
             :modules
             (map name)
             sort
             (str/join " "))
        " ]"])]])




(defn create-db [db-name]
  (js/console.log "CREATE DB" db-name)
  (let [k db-name
        dummy-db (-> (db/new-db {:modules [:annotations]}))
                                           ;(brainstorming/module)]}))
        root-id (db/root-id dummy-db)]
    (state/set! dbs k dummy-db nil)
    (state/set! roots k root-id nil)
    (state/set! cursors k root-id nil)))


(defn CreateDb [db-name]
  [mui/Card
   [:div
    "Database does not exist. "
    [button
     {:variant :contained
      :color :secondary
      :on-click #(create-db db-name)}
     "create"]]])


(defn Browser [navigation-match]
  [:div
   (if-let [db (db)]
     [:div.stack
      [EntitiesList [(root)]]]
     [:div.stack
      [CreateDb (db-name)]])])


(defn model []
  {:route ["/browser/:db-name"
           {:name :browser
            :view #'Browser
            :parameters {:path {:db-name string?}}}]})
