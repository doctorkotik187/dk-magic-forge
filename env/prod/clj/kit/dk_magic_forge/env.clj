(ns kit.dk-magic-forge.env
  (:require
    [clojure.tools.logging :as log]))


(def defaults
  {:init       (fn []
                 (log/info "\n-=[dk-magic-forge starting]=-"))
   :start      (fn []
                 (log/info "\n-=[dk-magic-forge started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[dk-magic-forge has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
