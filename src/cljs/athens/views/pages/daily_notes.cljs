(ns athens.views.pages.daily-notes
  (:require
    ["/components/Page/Page" :refer [DailyNotesPage]]
    ["@chakra-ui/react" :refer [VStack]]
    ["framer-motion" :refer [AnimatePresence]]
    [athens.dates :as dates]
    [athens.reactive :as reactive]
    [athens.views.pages.node-page :as node-page]
    [re-frame.core :refer [dispatch subscribe]]))


(defn reactive-pull-many
  "Need a reactive pull because block/uid doesn't exist yet in datascript, but is found in :daily-notes/items.
  This happens because (dispatch [:daily-note/next (dates/get-day)]) updates re-frame faster than the datascript tx can happen

  Bug: It's still possible for a day to not get created. The UI for this just shows an empty page without a title. Acceptable bug :)"
  [ids]
  (->> ids
       (keep #(reactive/get-reactive-block-document [:block/uid %]))
       (filter :block/uid)))


;; Components


(defn page
  []
  (let [note-refs (subscribe [:daily-notes/items])
        get-next-note #(dispatch [:daily-note/next (dates/get-day (dates/uid-to-date (last @note-refs)) 1)])]
    (fn []
      (if (empty? @note-refs)
        (dispatch [:daily-note/next (dates/get-day)])
        (let [notes (reactive-pull-many @note-refs)]
          [:> VStack {:alignSelf "stretch" :display "block" :py 16 :px [2 4 8] :spacing 8}
           [:> AnimatePresence {:initial false}
            (doall
              (for [{:keys [block/uid]} notes]
                [:> DailyNotesPage {:key uid
                                    ;; only the last gets onFirstAppear
                                    :onFirstAppear (if (= (last @note-refs) uid)
                                                     get-next-note
                                                     nil)}
                 [node-page/page [:block/uid uid]]]))]])))))
