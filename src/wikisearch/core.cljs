(ns wikisearch.core
  (:require
   ;; [ajax.core :refer [GET POST]]
   [cljs-http.client :as http]
   [clojure.string :as s]
   [reagent.core :as reagent :refer [atom]]
   [cljs.core.async :refer [<! chan sliding-buffer put! close! timeout]])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(enable-console-print!)

(def api-search "https://en.wikipedia.org/w/api.php")

(defonce search-term (atom nil))
(defonce search-results (atom nil))
(defonce search-results-extracts (atom []))

;; TODO for autocompletes use, eg., http://en.wikipedia.org/w/api.php?action=opensearch&search="ja"&format=json

(defn regen-extracts [term pages]
  (go (let [titles (s/join "|" (map :title pages)) 
            resp (<! (http/jsonp api-search {:query-params {:action "query"
                                                            :prop "extracts"
                                                            :exsentences 2
                                                            :exlimit "max"
                                                            :exintro ""
                                                            :format "json"
                                                            :titles titles}}))]
        ;; ignore when extracts corresponds to other search terms caused by async nature of ajax
        (when (= term @search-term)
          (if-let [pages (-> resp :body :query :pages)]
            (reset! search-results-extracts (vals pages))
            (reset! search-results-extracts []))))))

(defn do-search []
  (go (let [this-search @search-term
            resp-search (<! (http/jsonp api-search {:query-params {:action "query"
                                                                   :list "search"
                                                                   :srsearch this-search
                                                                   :format "json"}}))
            r-search (resp-search :body)]
        (when (= this-search @search-term)
          (reset! search-results r-search)
          (<! (regen-extracts this-search (-> r-search :query :search)))))))

(add-watch
 search-term
 :query-when-change
 (fn [k r o n]
   (if (or (not n)
           (< (count n) 4))
     (reset! search-results-extracts [])
     (when (and (not (s/blank? n))
                (> (count n) 3)
                (not= o n))
       (do-search)))))

(defn my-app []
  [:div.container-fluid
   [:div.text-center.search-title
    [:h1 "Wikipedia Search Engine"]]
   [:div.row.text-center
    [:div.col-lg-4.col-lg-offset-4.col-sm-8.col-sm-offset-2.text-left.search-line
     [:i.fa.fa-search]
     [:input.search-box {:placeholder "search term"
                         :value @search-term
                         :on-change #(reset! search-term (-> % .-target .-value))}]
     [:span.rand-page [:a {:href "http://en.wikipedia.org/wiki/Special:Random" :target "_blank"} "Random"]]]
    [:div.col-lg-8.col-lg-offset-2.col-sm-12.num-results
     (let [hits (-> @search-results :query :searchinfo :totalhits)]
       (when (and hits (pos? hits))
         [:h3 (str "Showing results 1 to 10 of " hits)]))]
    [:div.col-lg-8.col-lg-offset-2.col-sm-12.search-results-holdern
     (doall
      (for [{:keys [pageid title extract]} @search-results-extracts]
        ^{:keys pageid}
        [:div.search-result
         [:a {:href (str "http://en.wikipedia.org/?curid=" pageid)  :target "_blank"}
          [:h3 title]
          [:p {:dangerouslySetInnerHTML {:__html extract}}]]
         ]))
     ]]])

(defn main []
  (reagent/render [#'my-app] (.getElementById js/document "app")))

(main)
