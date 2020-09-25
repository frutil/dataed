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

   [frutil.db.query :as q]

   [frutil.dataed.browser.core :as browser]))




;;; commons

(defn ActionCard
  [{:keys [elevation on-click href color]}
   content]
  [card
   {:elevation elevation
    :style {:overflow :unset
            :background-color color}}
   [card-action-area
    {:on-click on-click
     :href href
     :class :height-100
     :component (if href :a :div)}
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


(defn Value [e a c v entity-path]
  [ActionCard
   {:elevation (elevation entity-path 2)
    :color (-> palette :value)
    :on-click #(mui/show-dialog
                [item-selector/Dialog
                 (browser/command-selector-options e a c v)])}
   (if (string? v)
     (if (= v "")
       [:span
        "<empty string>"]
       v)
     (mui/Data v))])


(declare Entity)


(defn Ref [parent-e a parent-c e entity-path]
  (if false ;;FIXME expanded?
    [Entity e (conj entity-path e)]
    [ActionCard
     {:elevation (elevation entity-path 2)
      :color (-> palette :value)
      :on-click #(mui/show-dialog
                  [item-selector/Dialog
                   (browser/command-selector-options parent-e a parent-c e)])}
     [:div.i
      (when (q/attribute-is-component? (browser/db) a)
        "> ")
      "#" e]]))


(defn AttributeValue [e a c v entity-path]
  (if (q/attribute-is-ref? (browser/db) a)
    ^{:key v} [Ref
               e a c (if (int? v)
                         v
                         (get v :db/id))
               entity-path]
    ^{:key v} [Value e a c v entity-path]))


(defn AttributeValuesCollection [e a vs entity-path]
  [TreeCardsWrapper
   [ActionCard
    {:elevation (elevation entity-path 2)
     :color (-> palette :value-seq)
     :on-click #(mui/show-dialog
                 [item-selector/Dialog
                  (browser/command-selector-options e a vs nil)])}
    [:div "⁞"]]
   (for [v vs]
     ^{:key v}
     [AttributeValue e a vs v entity-path])])


(defn Attribute [e a v entity-path]
  [:div
   {:style {:display :flex
            :gap "8px"}}
   [ActionCard
    {:elevation (elevation entity-path 1)
     :color (-> palette :attribute)
     :on-click #(mui/show-dialog
                 [item-selector/Dialog
                  (browser/command-selector-options e a nil nil)])}
    [mui/Caption a]]
   (if (q/attribute-is-many? (browser/db) a)
     [AttributeValuesCollection e a v entity-path]
     [AttributeValue e a nil v entity-path])])


(dev/spy (q/reverse-ref-attributes-idents (browser/db)))


(defn Entity [e entity-path]
  (if-let [entity (q/entity (browser/db) e)]
    (let [as (-> entity
                 keys
                 (into (q/reverse-ref-attributes-idents (browser/db)))
                 sort)
          as-nss (->> as
                      (remove q/attribute-is-reverse-ref?)
                      (map namespace)
                      (into #{}))
          on-click #(mui/show-dialog
                     [item-selector/Dialog
                      (browser/command-selector-options e nil nil nil)])]
      [:div.Entity
       [TreeCardsWrapper
        [ActionCard
         {:elevation (elevation entity-path 0)
          :on-click on-click
          :color (-> palette :entity)}
         [:div.stack
          [:div.i "#" e]
          (for [as-ns (sort as-nss)
                :let [[e n] (reverse (str/split as-ns #"\.(?=[^\.]+$)"))]]
            ^{:key as-ns}
            [:div
             (when n [:div {:style {:font-size "80%"}} n])
             [:div.b e]])]]
        (for [a as
              :let [v (get entity a)]
              :when v]
          ^{:key a}
          [Attribute e a v entity-path])]])
    [:div "Entity does not exist."]))


(defn EntitiesList [es]
  [:div.EntitiesList.stack.gap-2
   (for [e es]
     ^{:key e}
     [Entity e [e]])])


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
     [:div (browser/db-name)]
     (when-let [r (browser/root-e)] [:div "r: " r])
     (when-let [c (browser/cursor)] [:div "c: " c])
     [:div
      "[ "
      (->> (browser/modules)
           (map :ident)
           (map name)
           sort
           (str/join " "))
      " ]"]]])




(defn CreateDb [db-name]
  [mui/Card
   [:div
    "Database does not exist. "
    [button
     {:variant :contained
      :color :secondary
      :on-click #(browser/create-db db-name)}
     "create"]]])


(defn Browser [navigation-match]
  [:div
   (if-let [db (browser/db)]
     (let [root-e (get-in navigation-match [:parameters :path :root-e])]
       [EntitiesList
        (if (= root-e 0)
          (q/es-wheres db '[?e _ _])
          [root-e])])
     [CreateDb (browser/db-name)])])


(defn model []
  {:route ["/browser/:db-name/:root-e"
           {:name :browser
            :view #'Browser
            :parameters {:path {:db-name string?
                                :root-e int?}}}]})
