(ns kit.dk-magic-forge.web.routes.pages
  (:require
    [kit.dk-magic-forge.web.controllers.project :as project]
    [kit.dk-magic-forge.web.middleware.exception :as exception]
    [kit.dk-magic-forge.web.pages.layout :as layout]
    [integrant.core :as ig]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]))

(defn wrap-page-defaults []
  (let [error-page (layout/error-page
                     {:status 403
                      :title "Invalid anti-forgery token"})]
    #(wrap-anti-forgery % {:error-response error-page})))

(defn home [request]
  (layout/render request "home.html"))

(defn about [request]
  (layout/render request "about.html"))

(defn contact [request]
  (layout/render request "contact.html"))

(defn book-project [request]
  (layout/render request "book-project.html"
                 {:flash (:flash request)}))

;; Routes
(defn page-routes [opts]
  [["/" {:get home}]
   ["/about" {:get about}]
   ["/contact" {:get contact}]

   ["/book-project"
    {:get  book-project
     :post (partial project/create! opts)}]

   ["/inbox" {:get (partial project/list-inbox opts)}]
   ["/projects" {:get (partial project/list-projects opts)}]
   ["/someday" {:get (partial project/list-someday opts)}]
   ["/archives" {:get (partial project/list-archives opts)}]

   ["/project/:id" {:get (partial project/show opts)}]
   ["/project/:id/update" {:post (partial project/update! opts)}]])

(def route-data
  {:middleware
   [;; Default middleware for pages
    (wrap-page-defaults)
    ;; query-params & form-params
    parameters/parameters-middleware
    ;; encoding response body
    muuntaja/format-response-middleware
    ;; exception handling
    exception/wrap-exception]})

(derive :reitit.routes/pages :reitit/routes)

(defmethod ig/init-key :reitit.routes/pages
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (layout/init-selmer! opts)
  (fn [] [base-path route-data (page-routes opts)]))
