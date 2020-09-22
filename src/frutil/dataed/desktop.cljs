(ns frutil.dataed.desktop
  (:require
   [reagent.core :as r]

   [reagent-material-ui.core.app-bar :refer [app-bar]]
   [reagent-material-ui.core.toolbar :refer [toolbar]]
   [reagent-material-ui.core.container :refer [container]]

   [frutil.spa.devtools.core :as devtools]
   [frutil.spa.mui :as mui]
   [frutil.spa.navigation :as navigation]

   [frutil.dataed.browser :as browser]))


(defn Header []
  [app-bar
   {:position :static}
   [browser/Statusbar]])


(defn Content []
  [:div
   {:style {}}
   [mui/DialogsContainer]
   [:main.Workarea
    [navigation/Switcher :view]]])


(defn Footer []
  [:div])
   ;; [devtools/Console]])


(defn Desktop []
  [mui/Desktop--Header-Content-Footer
   [Header]
   [Content]
   [Footer]])
