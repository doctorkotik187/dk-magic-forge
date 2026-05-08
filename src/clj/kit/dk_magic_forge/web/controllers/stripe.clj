(ns kit.dk-magic-forge.web.controllers.stripe
  (:require [ring.util.response :as resp]
            [clojure.data.json :as json]
            [stripe-clojure.core :as stripe]
            [stripe-clojure.payment-intents :as pi]))

(def stripe-client
  (stripe/init-stripe {:api-key (System/getenv "STRIPE_API_KEY")
                       :api-version "2026-04-22.dahlia"}))

(defn create-payment-intent-handler [req]
  (let [body   (slurp (:body req))
        params (try
                 (json/read-str body :key-fn keyword)
                 (catch Exception _
                   {}))
        amount (get params :amount 500)
        currency (get params :currency "usd")]
    (try
      (let [intent (pi/create-payment-intent
                     stripe-client
                     {:amount      amount
                      :currency    currency
                      :description "Project Forge deposit"
                      :automatic_payment_methods {:enabled true}})]
        (-> (resp/response
              (json/write-str {:client_secret (:client_secret intent)
                               :id            (:id intent)}))
            (resp/content-type "application/json")))
      (catch Exception e
        (-> (resp/response
              (json/write-str {:error (.getMessage e)}))
            (resp/content-type "application/json")
            (resp/status 500))))))
