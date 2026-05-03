(ns kit.dk-magic-forge.web.controllers.project
  (:require
   [kit.dk-magic-forge.web.pages.layout :as layout]
   [ring.util.response :as response]
   [ring.util.http-response :as http-response]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; =========================
;; HELPERS
;; =========================

(def valid-lists #{"inbox" "projects" "someday" "archives"})  ;; Note the #
(def valid-states #{"doing" "todo" "waiting" "noop"})         ;; Note the #

(defn blank? [s]
  (or (nil? s) (str/blank? s)))

(defn format-date [ts]
  (when ts
    (subs (str ts) 0 10)))

(defn dwarf-group [state]
  (case (str/upper-case (or state ""))
    ("TODO" "DOING") "active"
    "WAITING" "waiting"
    "idle"))

(defn enrich-project [p]
  (-> p
      (assoc :dwarf_group (dwarf-group (:state p)))
      (assoc :dwarf (inc (rand-int 8)))
      (update :created_at format-date)
      (update :updated_at format-date)))

;; =========================
;; LIST VIEWS
;; =========================

(defn list-projects
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "projects"})
                      (map enrich-project))]
    (layout/render request "projects.html"
                   {:projects projects
                    :valid_lists valid-lists
                    :valid_states valid-states
                    :flash (:flash request)})))

(defn list-inbox
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "inbox"})
                      (map enrich-project))]
    (layout/render request "inbox.html"
                   {:projects projects
                    :valid_lists valid-lists
                    :valid_states valid-states
                    :flash (:flash request)})))

(defn list-someday
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "someday"})
                      (map enrich-project))]
    (layout/render request "someday.html"
                   {:projects projects
                    :valid_lists valid-lists
                    :valid_states valid-states
                    :flash (:flash request)})))

(defn list-archives
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "archives"})
                      (map enrich-project))]
    (layout/render request "archives.html"
                   {:projects projects
                    :valid_lists valid-lists
                    :valid_states valid-states
                    :flash (:flash request)})))

;; =========================
;; SINGLE PROJECT
;; =========================

(defn get-project
  [{:keys [query-fn]}
   {{:keys [id]} :path-params :as request}]

  (if-let [project (query-fn :get-project {:id id})]
    (layout/render request "project.html"
                   {:project (enrich-project project)
                    :flash (:flash request)})
    (layout/render request "404.html"
                   {:error "Project not found"
                    :flash (:flash request)})))

;; =========================
;; CREATE PROJECT
;; =========================

(defn create-project!
  [{:keys [query-fn]}
   {{:strs [project_name project_description tech_stack streaming_option]}
    :form-params
    :as _request}]

  (log/debug "FORM:" project_name project_description tech_stack streaming_option)

  (let [errors (cond-> {}
                 (blank? project_name)
                 (assoc :project_name "Project name is required")

                 (blank? project_description)
                 (assoc :project_description "Description is required"))

        is-public (boolean streaming_option)

        project-data {:title project_name
                      :description project_description
                      :programming_lang (or tech_stack "clojure")
                      :is_open_source is-public
                      :client_budget_cents (if is-public 50 100)}]

    (if (seq errors)
      (-> (response/redirect "/book-project")
          (assoc :flash {:errors errors}))

      (try
        (query-fn :create-project! project-data)

        (-> (response/redirect "/inbox")
            (assoc :flash {:message "Project forged successfully ⚒"}))

        (catch Exception e
          (log/error e "Project creation failed")
          (-> (response/redirect "/book-project")
              (assoc :flash {:errors {:unknown (.getMessage e)}})))))))

;; =========================
;; UPDATES
;; =========================

(defn update-project!
  [{:keys [query-fn]}
   {{:keys [id]} :path-params
    {:strs [list state]} :form-params}]
  (log/debug "Valid lists:" valid-lists "Valid states:" valid-states)
  (log/debug "Got list:" list "state:" state)
  (cond
    (or (not (contains? valid-lists list))
        (not (contains? valid-states state)))
    (do
      (log/debug "Validation failed for" list state)
      (-> (response/redirect (str "/" list))  ;; Use list even on error
          (assoc :flash {:error "Invalid list or state specified."})))

    :else
    (do
      (log/info "Updating project" id "to list:" list "state:" state)
      (query-fn :update-project! {:id (parse-long id) :list list :state state})
      (-> (http-response/found (str "/" list))
          (assoc :flash {:message "Project updated successfully."})))))
