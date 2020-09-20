(ns frutil.dataed.desktop
  (:require
   [reagent.core :as r]

   [reagent-material-ui.core.app-bar :refer [app-bar]]
   [reagent-material-ui.core.toolbar :refer [toolbar]]
   [reagent-material-ui.core.container :refer [container]]

   [frutil.spa.devtools.core :as devtools]
   [frutil.spa.mui :as mui]

   [frutil.dataed.browser :as browser]))


(defn AppBar []
  [app-bar
   {:position :fixed}
   [browser/Statusbar]])


(defn Desktop []
  [:<>
   [mui/DialogsContainer]
   [AppBar]
   [container
    [:div.toolbar]
    [:br]
    [:main
     [browser/Browser]]]
   [devtools/Console]])
