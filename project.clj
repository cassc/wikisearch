(defproject wikisearch "0.1.1-SNAPSHOT"
  :description "The original figwheel flappybird demo"

  :url "http://rigsomelight.com/2014/05/01/interactive-programming-flappy-bird-clojurescript.html"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [org.clojure/core.async "0.2.395"]
                 ;; [cljs-ajax "0.5.8"]
                 [cljs-http "0.1.42"]
                 [reagent "0.5.1"]]

  :plugins [[lein-cljsbuild "1.1.4"
             :exclusions [org.clojure/clojure]]
            [lein-figwheel "0.5.4-7"]]

  :clean-targets ^{:protect false} ["resources/public/js/out"
                                    "resources/public/cljs"
                                    "resources/public/js/wikisearch.js"
                                    :target-path]

  :source-paths ["src"]

  :profiles {:prod {:hooks [leiningen.cljsbuild]
                    :cljsbuild {:builds {:app
                                         {:figwheel false
                                          :compiler ^{:replace true}
                                          {:output-to "resources/public/cljs/wikisearch.js"
                                           :output-dir "resources/public/cljs/prod"
                                           :optimizations :advanced ;; :whitespace :advanced
                                           :source-map "resources/public/cljs/wikisearch.map"
                                           :pretty-print false}}}}}
             :dev {:env {:dev true}
                   :dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.2"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}
  :cljsbuild {:builds {:app
                       {:source-paths ["src"]
                        :figwheel true
                        :compiler {:main wikisearch.core
                                   :asset-path "js/out"
                                   :output-to "resources/public/cljs/wikisearch.js"
                                   :output-dir "resources/public/cljs/out"
                                   :source-map-timestamp true}}}}

  :figwheel { :css-dirs ["resources/public/css"]
             :open-file-command "emacsclient"
             })
