(ns kit.dk-magic-forge.web.middleware.core
  (:require
   [kit.dk-magic-forge.env :as env]
   [ring.middleware.defaults :as defaults]
   [ring.middleware.file :refer [wrap-file]]
   [ring.middleware.session.cookie :as cookie]))

(defn wrap-base
  [{:keys [metrics site-defaults-config cookie-secret] :as opts}]
  (let [cookie-store (cookie/cookie-store {:key (.getBytes ^String cookie-secret)})]
    (fn [handler]
      (-> handler

          ;; ==========================================
          ;; STATIC FILE SERVING (PDFs, assets, etc.)
          ;; ==========================================
          (wrap-file "storage")

          ;; Kit default middleware stack
          ((:middleware env/defaults) opts)

          ;; Ring defaults (sessions, security, etc.)
          (defaults/wrap-defaults
           (assoc-in site-defaults-config
                     [:session :store]
                     cookie-store))))))
