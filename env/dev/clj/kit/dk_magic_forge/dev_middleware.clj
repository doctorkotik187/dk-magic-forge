(ns kit.dk-magic-forge.dev-middleware
  (:require
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.keyword-params :refer [wrap-keyword-params]]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]))

(defn wrap-dev [handler _opts]
  (-> handler
      wrap-keyword-params
      wrap-params
      wrap-multipart-params))
