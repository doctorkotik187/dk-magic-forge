(ns kit.dk-magic-forge.web.controllers.project
  (:require
   [clojure.tools.logging :as log]
   [kit.dk-magic-forge.web.pages.layout :as layout]
   [ring.util.http-response :as http]))

;; -------------------------
;; Helpers
;; -------------------------

(defn base-project [params]
  {:title (:title params)
   :description (:description params)
   :list (or (:list params) "inbox")
   :state (or (:state params) "todo")
   :client_budget_cents (:client_budget_cents params)})

;; -------------------------
;; Queries
;; -------------------------

(defn list-projects
  [{:keys [query-fn]} request]
  (layout/render request
                 "projects.html"
                 {:projects (query-fn :get-projects {})}))

(defn get-project
  [{:keys [query-fn]} {{:keys [id]} :path-params}]
  (if-let [project (query-fn :get-project {:id id})]
    (http/ok project)
    (http/not-found {:error "Project not found"})))

;; -------------------------
;; Create
;; -------------------------

(defn create-project!
  [{:keys [query-fn]} {:keys [params]}]
  (try
    (let [project (base-project params)]
      (query-fn :create-project! project)
      (http/found "/projects"))
    (catch Exception e
      (log/error e "Failed to create project")
      (http/internal-server-error
       {:error "Failed to create project"}))))

;; -------------------------
;; State transitions (GTD core)
;; -------------------------

(defn update-state!
  [{:keys [query-fn]} {{:keys [id]} :path-params {:keys [state]} :params}]
  (query-fn :update-state! {:id id :state state})
  (http/found "/projects"))

(defn update-list!
  [{:keys [query-fn]} {{:keys [id]} :path-params {:keys [list]} :params}]
  (query-fn :update-list! {:id id :list list})
  (http/found "/projects"))
