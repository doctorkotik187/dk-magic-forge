(ns kit.dk-magic-forge.web.controllers.project
  (:require
   [kit.dk-magic-forge.web.pages.layout :as layout]
   [ring.util.response :as response]
   [ring.util.http-response :as http-response]
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

;; ------------------------------------------------------------
;; constants
;; ------------------------------------------------------------

(def valid-lists #{"inbox" "projects" "someday" "archives"})
(def valid-states #{"doing" "todo" "waiting" "noop" "done" "canceled"})
(def valid-priorities #{"a" "b" "c" "d"})

;; ------------------------------------------------------------
;; helpers
;; ------------------------------------------------------------

(defn blank? [s]
  (or (nil? s) (str/blank? s)))

(defn parse-long-safe [s]
  (when-not (blank? s)
    (try
      (parse-long s)
      (catch Exception _ nil))))

(defn format-date [ts]
  (when ts
    (subs (str ts) 0 10)))

(defn cents->dollars [cents]
  (when (some? cents)
    (/ cents 100.0)))

(defn minutes->hours [minutes]
  (when (some? minutes)
    (/ minutes 60.0)))

(defn dwarf-group [state]
  (let [s (str/lower-case (or state ""))]
    (cond
      (#{"todo" "doing"} s) "active"
      (= "waiting" s) "waiting"
      :else "idle")))

(defn present-keys [m]
  (into {} (remove (comp nil? val) m)))

;; ------------------------------------------------------------
;; core view model
;; ------------------------------------------------------------

(defn enrich-project [p]
  (-> p
      (assoc :dwarf_group (dwarf-group (:state p)))
      (assoc :dwarf (inc (rand-int 8)))
      (update :created_at format-date)
      (update :updated_at format-date)
      (assoc :hours_worked_display (minutes->hours (:minutes_worked p)))
      (assoc :hourly_rate_display (cents->dollars (:hourly_rate_cents p)))))

;; ------------------------------------------------------------
;; shared list render
;; ------------------------------------------------------------

(defn render-project-list [request template projects]
  (layout/render request template
                 {:projects projects
                  :valid_lists valid-lists
                  :valid_states valid-states
                  :valid_priorities valid-priorities
                  :flash (:flash request)}))

;; ------------------------------------------------------------
;; list views
;; ------------------------------------------------------------

(defn list-projects
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "projects"})
                      (map enrich-project))]
    (render-project-list request "projects.html" projects)))

(defn list-inbox
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "inbox"})
                      (map enrich-project))]
    (render-project-list request "inbox.html" projects)))

(defn list-someday
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "someday"})
                      (map enrich-project))]
    (render-project-list request "someday.html" projects)))

(defn list-archives
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "archives"})
                      (map enrich-project))]
    (render-project-list request "archives.html" projects)))

;; ------------------------------------------------------------
;; single project
;; ------------------------------------------------------------

(defn show
  [{:keys [query-fn]}
   {{:keys [id]} :path-params :as request}]
  (if-let [project-id (parse-long-safe id)]
    (if-let [project (query-fn :get-project {:id project-id})]
      (layout/render request "project/show.html"
                     {:project (enrich-project project)
                      :flash (:flash request)})
      (layout/render request "404.html"
                     {:error "Project not found"
                      :flash (:flash request)}))
    (layout/render request "404.html"
                   {:error "Invalid project ID"
                    :flash (:flash request)})))

(defn edit
  [{:keys [query-fn]}
   {{:keys [id]} :path-params :as request}]
  (if-let [project-id (parse-long-safe id)]
    (if-let [project (query-fn :get-project {:id project-id})]
      (layout/render request "project/edit.html"
                     {:project (enrich-project project)
                      :flash (:flash request)})
      (layout/render request "404.html"
                     {:error "Project not found"
                      :flash (:flash request)}))
    (layout/render request "404.html"
                   {:error "Invalid project ID"
                    :flash (:flash request)})))

;; ------------------------------------------------------------
;; create
;; ------------------------------------------------------------

(defn create!
  [{:keys [query-fn]}
   {{:strs [project_title
            project_description
            tech_stack
            has_test_suite
            is_open_source
            hourly_rate]}
    :form-params
    :as _request}]
  (log/debug "FORM:"
             project_title
             project_description
             tech_stack
             has_test_suite
             is_open_source
             hourly_rate)
  (let [errors (cond-> {}
                 (blank? project_title)
                 (assoc :project_title "Project title is required")

                 (blank? project_description)
                 (assoc :project_description "Description is required"))

        test-suite? (some? has_test_suite)
        open-source? (some? is_open_source)
        hourly-rate (parse-long-safe hourly_rate)
        hourly-rate-cents (some-> hourly-rate (* 100))
        project-data {:title project_title
                      :description project_description
                      :is_personal false
                      :programming_lang (or tech_stack "clojure")
                      :has_test_suite test-suite?
                      :is_open_source open-source?
                      :hourly_rate_cents hourly-rate-cents}]
    (if (seq errors)
      (-> (response/redirect "/booking")
          (assoc :flash {:errors errors}))
      (try
        (query-fn :create-project! project-data)
        (-> (response/redirect "/inbox")
            (assoc :flash {:message "Project forged successfully ⚒"}))
        (catch Exception e
          (log/error e "Project creation failed")
          (-> (response/redirect "/booking")
              (assoc :flash {:errors {:unknown (.getMessage e)}})))))))

;; ------------------------------------------------------------
;; update
;; ------------------------------------------------------------

(defn update!
  [{:keys [query-fn]}
   {{:keys [id]} :path-params
    :as request}]
  (let [params (:form-params request)
        project-id (parse-long-safe id)
        list (get params "list")
        state (get params "state")
        priority (get params "priority")
        hourly-rate (parse-long-safe (get params "hourly_rate"))
        hours-worked (parse-long-safe (get params "hours_worked"))
        errors (cond-> {}
                 (and list (not (contains? valid-lists list)))
                 (assoc :list "Invalid list specified")

                 (and state (not (contains? valid-states state)))
                 (assoc :state "Invalid state specified")

                 (and priority (not (contains? valid-priorities priority)))
                 (assoc :priority "Invalid priority specified"))

        patch (present-keys
               {:title (get params "title")
                :description (get params "description")
                :details (get params "details")
                :is_personal (get params "is_personal")
                :programming_lang (get params "programming_lang")
                :has_test_suite (get params "has_test_suite")
                :is_open_source (get params "is_open_source")
                :list list
                :state state
                :priority priority
                :hourly_rate_cents (some-> hourly-rate (* 100))
                :minutes_worked hours-worked})]
    (cond
      (nil? project-id)
      (-> (response/redirect "/inbox")
          (assoc :flash {:error "Invalid project ID"}))

      (seq errors)
      (-> (response/redirect (str "/" (or list "inbox")))
          (assoc :flash {:errors errors}))

      :else
      (do
        (log/info "Updating project" project-id "with patch" patch)
        (query-fn :update-project! (assoc patch :id project-id))
        (-> (http-response/found (str "/" (or list "inbox")))
            (assoc :flash {:message "Project updated successfully."}))))))
