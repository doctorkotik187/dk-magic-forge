(ns kit.dk-magic-forge.web.routes.api
  (:require
   [integrant.core :as ig]
   [kit.dk-magic-forge.web.controllers.health :as health]
   [kit.dk-magic-forge.web.controllers.stripe :as stripe]
   [kit.dk-magic-forge.web.middleware.exception :as exception]
   [kit.dk-magic-forge.web.middleware.formats :as formats]
   [reitit.coercion.malli :as malli]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.swagger :as swagger]))

(def route-data
  {:coercion   malli/coercion
   :muuntaja   formats/instance
   :swagger    {:id ::api}
   :middleware [;; query-params & form-params
                parameters/parameters-middleware
                ;; content-negotiation
                muuntaja/format-negotiate-middleware
                ;; encoding response body
                muuntaja/format-response-middleware
                ;; exception handling
                coercion/coerce-exceptions-middleware
                ;; decoding request body
                muuntaja/format-request-middleware
                ;; coercing response bodys
                coercion/coerce-response-middleware
                ;; coercing request parameters
                coercion/coerce-request-middleware
                ;; exception handling
                exception/wrap-exception]})

;; Routes
(defn api-routes
  [_opts]
  [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title "kit.dk-magic-forge API"}}
           :handler (swagger/create-swagger-handler)}}]
   ["/health" {:get #'health/healthcheck!}]
   ["/create-payment-intent" {:post stripe/create-payment-intent-handler}]

   ])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (fn [] [base-path route-data (api-routes opts)]))
