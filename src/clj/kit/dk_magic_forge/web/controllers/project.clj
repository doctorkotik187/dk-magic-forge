(ns kit.dk-magic-forge.web.controllers.project
  (:require
   [kit.dk-magic-forge.web.pages.layout :as layout]
   [ring.util.response :as response]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; -------------------------
;; Helpers
;; -------------------------

(defn- blank? [s]
  (or (nil? s) (str/blank? s)))

(defn- uuid []
  (java.util.UUID/randomUUID))

;; -------------------------
;; Queries
;; -------------------------

(defn list-projects
  [{:keys [query-fn]} request]
  (layout/render request "projects.html"
                 {:projects (query-fn :get-projects {})}))

(defn get-project
  [{:keys [query-fn]} {{:keys [id]} :path-params :as request}]
  (if-let [project (query-fn :get-project {:id id})]
    (layout/render request "project.html" {:project project})
    (layout/render request "404.html" {:error "Project not found"})))

;; -------------------------
;; Create
;; -------------------------

(defn create-project!
  [{:keys [query-fn]}
   {{:strs [project_name project_description tech_stack streaming_option]}
    :form-params
    :as request}]

  (log/debug "FORM PARAMS:" project_name project_description tech_stack streaming_option)

  (let [errors (cond-> {}
                 (blank? project_name)
                 (assoc :project_name "Project name is required")

                 (blank? project_description)
                 (assoc :project_description "Description is required"))

        is-public (boolean streaming_option)

        project-data {:id (uuid)
                      :title project_name
                      :description project_description
                      :programming_lang (or tech_stack "clojure")
                      :is_open_source is-public
                      :list "booked"
                      :state "pending"
                      :client_budget_cents (if is-public 50 100)}]

    (if (seq errors)
      (-> (response/redirect "/projects/new")
          (assoc :flash {:errors errors}))

      (try
        (log/debug "INSERTING PROJECT:" project-data)
        (query-fn :create-project! project-data)

        (-> (response/redirect "/projects")
            (assoc :flash {:message
                           (str "Project '" project_name "' ("
                                (if is-public "🔓 Public" "🔒 Private")
                                ") created!")}))
        (catch Exception e
          (log/error e "Failed to create project")
          (-> (response/redirect "/projects/new")
              (assoc :flash {:errors {:unknown (.getMessage e)}})))))))

;; -------------------------
;; Updates
;; -------------------------

(defn update-state!
  [{:keys [query-fn]}
   {{:keys [id]} :path-params
    {:keys [state]} :form-params}]

  (log/debug "UPDATE STATE:" id state)

  (query-fn :update-state! {:id id :state state})

  (-> (response/redirect "/projects")
      (assoc :flash {:message (str "State → " state)})))

(defn update-list!
  [{:keys [query-fn]}
   {{:keys [id]} :path-params
    {:keys [list]} :form-params}]

  (log/debug "UPDATE LIST:" id list)

  (query-fn :update-list! {:id id :list list})

  (-> (response/redirect "/projects")
      (assoc :flash {:message (str "List → " list)})))
