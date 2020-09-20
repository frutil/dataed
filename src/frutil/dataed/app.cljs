(ns ^:figwheel-hooks frutil.dataed.app
  (:require
   [reagent-material-ui.colors :as colors]

   [frutil.spa.mui :as mui]

   [frutil.dataed.desktop :as desktop]))



(def theme
  {:palette {:primary {:main (get colors/purple 900)}
             :secondary {:main (get colors/pink 600)}}})


(defn styles [{:keys [spacing] :as theme}]
  ;; TODO (js/console.log "THEME" (-> theme :mixins))
  {"& .toolbar" (-> theme :mixins :toolbar)

   ;; TODO move to spa
   ;; common
   "& .b" {:font-weight :bold :letter-spacing "1px"}
   "& .monospace textarea" {:font-family :monospace}
   "& .stack" {:display :flex
               :flex-direction :column
               :gap (str (spacing 1) "px")}
   "& .flex" {:display :flex
              :gap (str (spacing 1) "px")}})


(defn mount-app []
  (mui/mount-app theme styles #'desktop/Desktop))


(defn initialize []
  (mount-app))


(defonce initialized
  (do
    (initialize)
    true))


(defn ^:after-load dev-after-load []
  (initialize))
