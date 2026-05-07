(ns kit.dk-magic-forge.web.controllers.payment
  (:require
    [ring.util.http-response :as http-response]
    [clojure.core :refer [parse-long]]))

(defn create!
  [{:keys [query-fn]}
   {{:keys [project-id]} :path-params
    :as request}]

  (let [params (:params request)

        project-id (parse-long project-id)
        original-amount (parse-long (:amount_cents params))

        usd-rate (parse-long (or (:usd_rate params) "1"))
        usd-amount (long (* original-amount usd-rate))

        ;; DATE string: "YYYY-MM-DD" or nil
        paid-at (not-empty (:paid_at params))]

    (query-fn :create-payment!
              {:project_id project-id
               :invoice_number (:invoice_number params)
               :original_amount_cents original-amount
               :original_currency (:currency params)
               :usd_amount_cents usd-amount
               :note (:note params)

               ;; now explicitly DATE-compatible
               :paid_at paid-at})

    (http-response/found (str "/project/" project-id))))


(defn delete!
  [{:keys [query-fn]}
   {{:keys [project-id payment-id]} :path-params}]

  (let [project-id (parse-long project-id)
        payment-id (parse-long payment-id)]

    (query-fn :delete-payment!
              {:id payment-id
               :project_id project-id})

    (http-response/found (str "/project/" project-id))))
