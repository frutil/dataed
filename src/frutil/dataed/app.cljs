(ns ^:figwheel-hooks frutil.dataed.app
  (:require
   [reagent-material-ui.colors :as colors]

   [frutil.spa.navigation :as navigation]
   [frutil.spa.mui :as mui]

   ;; browser modules
   [frutil.dataed.browser.modules.browser]
   [frutil.dataed.browser.modules.editor]
   [frutil.dataed.browser.modules.annotations]

   [frutil.dataed.desktop :as desktop]
   [frutil.dataed.browser :as browser]))


(def theme
  ;; https://material.io/resources/color/#!/?view.left=0&view.right=0&primary.color=4A148C&secondary.color=D81B60
  {:palette {:primary {:main (get colors/grey 800)}
             :secondary {:main (get colors/lime 600)}
             :background {:default "#E1E2E1"}}})


(defn styles [{:keys [spacing] :as theme}]
  {"& .MuiTypography-caption" {:color (-> theme :palette :primary :light)}
   "& .Workarea" {:padding (spacing 2)}

   ;; TODO move to spa
   ;; common
   "& .gap" {:gap (str (spacing 1) "px")}
   "& .gap-2" {:gap (str (spacing 2) "px")}
   "& .i" {:font-style :italic}
   "& .b" {:font-weight :bold}
          :letter-spacing "1px"
   "& .monospace" {:font-family :monospace}
   "& .monospace textarea" {:font-family :monospace}
   "& .stack" {:display :flex
               :flex-direction :column
               :gap (str (spacing 1) "px")}
   "& .flex" {:display :flex
              :gap (str (spacing 1) "px")}
   "& .sticky" {:position :sticky
                :top 0
                :align-self :flex-start}
   "& .height-100" {:height "100%"}})


(defn mount-app []
  (mui/mount-app theme styles #'desktop/Desktop))


(defn initialize []
  (navigation/initialize! [(-> (browser/model) :route)])
  (mount-app))


(defonce initialized
  (do
    (initialize)
    true))


(defn ^:after-load dev-after-load []
  (initialize))
