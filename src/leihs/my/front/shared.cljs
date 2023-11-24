(ns leihs.my.front.shared
  (:refer-clojure :exclude [str keyword])
  (:require
   ["date-fns" :as date-fns]
   [goog.string :as gstring]
   [leihs.core.core :refer [keyword str presence]]
   [leihs.core.digest]
   [leihs.my.front.state :as state]))

(defn humanize-datetime [ref_dt dt add-suffix]
  [:span (date-fns/formatDistance
          dt ref_dt
          (clj->js {:addSuffix add-suffix}))])

(defn humanize-datetime-component [dt & {:keys [add-suffix]
                                         :or {add-suffix true}}]
  (if-let [dt (if (string? dt) (js/Date. dt) dt)]
    [:span.datetime
     {:data-iso8601 (.toISOString dt)}
     ;[:pre (with-out-str (pprint (.toISOString dt)))]
     [humanize-datetime (:timestamp @state/global-state*) dt add-suffix]]
    [:span "NULL"]))

(defn short-id [uuid]
  [:span {:style {:font-family :monospace}}
   (->> uuid (take 8) clojure.string/join)])

(defn gravatar-url
  ([email]
   (gravatar-url email 32))
  ([email size]
   (if-not (presence email)
     (gstring/format
      "https://www.gravatar.com/avatar/?s=%d&d=blank" size)
     (let [md5 (->> email
                    clojure.string/trim
                    clojure.string/lower-case
                    leihs.core.digest/md5-hex)]
       (gstring/format
        "https://www.gravatar.com/avatar/%s?s=%d&d=retro"
        md5 size)))))

