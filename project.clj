(defproject req-gen "0.1.0-SNAPSHOT"
  :description "Zendesk App Creator"
  :url "https://github.com/atroche/zendesk_app_creator"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2342"]
                 [ring "1.3.1"]
                 [prismatic/schema "0.3.0"]
                 [compojure "1.1.9"]
                 [enlive "1.1.5"]
                 [om "0.7.3"]
                 [figwheel "0.1.4-SNAPSHOT"]
                 [environ "1.0.0"]
                 [com.cemerick/piggieback "0.1.3"]
                 [weasel "0.4.0-SNAPSHOT"]
                 [cheshire "5.3.1"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [prismatic/om-tools "0.3.3"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [com.cemerick/austin "0.1.5"]
            [lein-environ "1.0.0"]]

  :min-lein-version "2.0.0"

  :uberjar-name "req-gen.jar"

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/app.js"
                                        :output-dir    "resources/public/out"
                                        :source-map    "resources/public/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :none
                                        :pretty-print  true}}
                      :prod {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/prod.js"
                                        :output-dir    "resources/public/out-prod"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :advanced
                                        :pretty-print  false}}}}

  :profiles {:dev {:repl-options {:init-ns req-gen.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :plugins [[lein-figwheel "0.1.4-SNAPSHOT"]]
                   :figwheel {:http-server-root "public"
                              :port 3449 }
                   :env {:is-dev true}}

             :uberjar {:hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
