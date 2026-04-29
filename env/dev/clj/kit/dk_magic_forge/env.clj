(ns kit.dk-magic-forge.env
  (:require
    [clojure.tools.logging :as log]
    [kit.dk-magic-forge.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[dk-magic-forge starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[dk-magic-forge started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[dk-magic-forge has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev}})
