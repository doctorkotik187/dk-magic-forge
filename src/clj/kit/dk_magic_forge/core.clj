(ns kit.dk-magic-forge.core
  (:gen-class)
  (:require
   [clojure.tools.logging :as log]
   [integrant.core :as ig]
   [kit.dk-magic-forge.config :as config]
   [kit.dk-magic-forge.env :refer [defaults]]
   [kit.dk-magic-forge.web.handler]
    ;; Routes
   [kit.dk-magic-forge.web.routes.api]
   [kit.dk-magic-forge.web.routes.pages]
   [kit.edge.db.postgres]
   [kit.edge.db.sql.conman]
   [kit.edge.db.sql.migratus]
    ;; Edges
   [kit.edge.server.undertow]))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
 (fn [thread ex]
   (log/error {:what :uncaught-exception
               :exception ex
               :where (str "Uncaught exception on" (.getName thread))})))

(defonce system (atom nil))

(defn stop-app
  []
  ((or (:stop defaults) (fn [])))
  (some-> (deref system) (ig/halt!)))

(defn start-app
  [& [params]]
  ((or (:start params) (:start defaults) (fn [])))
  (->> (config/system-config (or (:opts params) (:opts defaults) {}))
       (ig/expand)
       (ig/init)
       (reset! system)))

(defn -main
  [& _]
  (start-app)
  (.addShutdownHook (Runtime/getRuntime) (Thread. (fn [] (stop-app) (shutdown-agents)))))
