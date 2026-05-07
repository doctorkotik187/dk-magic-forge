(ns kit.dk-magic-forge.web.controllers.contact
  (:require
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [postal.core :refer [send-message]]
   [ring.util.response :as response]))

(def smtp-config
  {:host "localhost"
   :port 1025
   :tls false
   :user nil
   :pass nil
   :from "forge-contact@dk-magic-forge.dev"
   :to   "dev@example.com"})

(defn blank?
  [s]
  (or (nil? s) (str/blank? s)))

(defn submit-contact!
  [_ ;; no need for `{:keys [config]}` for now
   {{:strs [name email message]} :form-params}]
  (let [errors (cond-> {}
                 (blank? name)    (assoc :name "Name is required")
                 (blank? email)   (assoc :email "Email is required")
                 (blank? message) (assoc :message "Message is required"))]
    (if (seq errors)
      (-> (response/redirect "/contact")
          (assoc :flash {:errors errors}))

      (try
        (send-message smtp-config
                      {:from email
                       :to (:to smtp-config)
                       :subject (str "Forge Message from " name)
                       :body (str "Name: " name "\n"
                                  "Email: " email "\n\n"
                                  message)})

        (-> (response/redirect "/contact")
            (assoc :flash {:message "Message sent into the forge 🔥"}))

        (catch Exception e
          (log/error e "Failed to send contact email")
          (-> (response/redirect "/contact")
              (assoc :flash {:error "Failed to send message"})))))))
