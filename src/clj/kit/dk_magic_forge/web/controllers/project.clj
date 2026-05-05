(ns kit.dk-magic-forge.web.controllers.project
  (:require
   [kit.dk-magic-forge.web.pages.layout :as layout]
   [ring.util.response :as response]
   [ring.util.http-response :as http-response]
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log])
  (:import
   [java.util UUID]))

(def valid-lists #{"inbox" "projects" "someday" "archives"})
(def valid-states #{"doing" "todo" "waiting" "noop" "done" "canceled"})
(def valid-priorities #{"a" "b" "c" "d"})

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

(defn checkbox-checked? [params k]
  (contains? params k))

(defn render-project-list [request template projects]
  (layout/render request template
                 {:projects projects
                  :valid_lists valid-lists
                  :valid_states valid-states
                  :valid_priorities valid-priorities
                  :flash (:flash request)}))

(defn enrich-project [p]
  (-> p
      (assoc :dwarf_group (dwarf-group (:state p)))
      (assoc :dwarf (inc (rand-int 8)))
      (update :created_at format-date)
      (update :updated_at format-date)
      (assoc :hours_worked_display (minutes->hours (:minutes_worked p)))
      (assoc :max_budget_display (cents->dollars (:max_budget_cents p)))
      (assoc :hourly_rate_display (cents->dollars (:hourly_rate_cents p)))))

(defn project-id-or-nil [id]
  (parse-long-safe id))

(defn project-patch [params]
  {:title (get params "title")
   :description (get params "description")
   :details (get params "details")
   :programming_lang (get params "programming_lang")
   :is_personal (checkbox-checked? params "is_personal")
   :has_test_suite (checkbox-checked? params "has_test_suite")
   :is_open_source (checkbox-checked? params "is_open_source")
   :list (get params "list")
   :state (get params "state")
   :priority (get params "priority")
   :max_budget_cents (some-> (get params "max_budget") parse-long-safe (* 100))
   :hourly_rate_cents (some-> (get params "hourly_rate") parse-long-safe (* 100))
   :minutes_worked (some-> (get params "hours_worked") parse-long-safe (* 60))})

(defn validate-patch [patch]
  (cond-> {}
    (and (:list patch) (not (contains? valid-lists (:list patch))))
    (assoc :list "Invalid list specified")

    (and (:state patch) (not (contains? valid-states (:state patch))))
    (assoc :state "Invalid state specified")

    (and (:priority patch) (not (contains? valid-priorities (:priority patch))))
    (assoc :priority "Invalid priority specified")))

;; =========================================================
;; PROJECT LISTS
;; =========================================================

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

;; =========================================================
;; PROJECT VIEW
;; =========================================================

(defn show
  [{:keys [query-fn]}
   {{:keys [id]} :path-params :as request}]

  (if-let [project-id (project-id-or-nil id)]
    (if-let [project (query-fn :get-project {:id project-id})]

      (let [files (query-fn :get-project-files {:project_id project-id})]

        (layout/render request "project/show.html"
                       {:project (enrich-project project)
                        :files files
                        :flash (:flash request)}))

      (layout/render request "404.html"
                     {:error "Project not found"
                      :flash (:flash request)}))

    (layout/render request "404.html"
                   {:error "Invalid project ID"
                    :flash (:flash request)})))

(defn edit
  [{:keys [query-fn]}
   {{:keys [id]} :path-params :as request}]
  (if-let [project-id (project-id-or-nil id)]
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

;; =========================================================
;; CREATE / UPDATE
;; =========================================================

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
  (let [errors (cond-> {}
                 (blank? project_title)
                 (assoc :project_title "Project title is required")

                 (blank? project_description)
                 (assoc :project_description "Description is required"))

        project-data {:title project_title
                      :description project_description
                      :is_personal false
                      :programming_lang (or tech_stack "clojure")
                      :has_test_suite (some? has_test_suite)
                      :is_open_source (some? is_open_source)
                      :hourly_rate_cents (some-> hourly_rate parse-long-safe (* 100))}]
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

(defn update!
  [{:keys [query-fn]}
   {{:keys [id]} :path-params
    :as request}]
  (let [params (:form-params request)
        project-id (project-id-or-nil id)
        patch (project-patch params)
        errors (validate-patch patch)]
    (cond
      (nil? project-id)
      (-> (response/redirect "/inbox")
          (assoc :flash {:error "Invalid project ID"}))

      (seq errors)
      (-> (response/redirect (str "/" (or (:list patch) "inbox")))
          (assoc :flash {:errors errors}))

      :else
      (do
        (log/info "Updating project" project-id "with patch" patch)
        (query-fn :update-project! (assoc patch :id project-id))
        (-> (http-response/found (str "/" (or (:list patch) "inbox")))
            (assoc :flash {:message "Project updated successfully."}))))))

;; =========================================================
;; UPLOAD (FIXED)
;; =========================================================

(defn upload!
  [{:keys [query-fn]}
   {{:keys [id]} :path-params
    :as request}]

  (let [params (:params request)
        upload (get params :pdf)
        project-id (project-id-or-nil id)]

    (println "PROJECT-ID:" project-id)
    (println "UPLOAD:" upload)

    (if (and project-id upload)

      (let [{:keys [filename content-type tempfile size]} upload
            safe-filename (str (UUID/randomUUID) ".pdf")
            dest-dir (java.io.File. (str "storage/pdfs/" project-id))
            dest-file (java.io.File. dest-dir safe-filename)]

        (.mkdirs dest-dir)

        (with-open [in (io/input-stream tempfile)
                    out (io/output-stream dest-file)]
          (io/copy in out))

        (try
          (query-fn :create-project-file!
                    {:project_id project-id
                     :original_filename filename
                     :stored_filename safe-filename
                     :content_type content-type
                     :storage_path (.getPath dest-file)
                     :file_size size})

          (-> (response/redirect (str "/project/" project-id))
              (assoc :flash {:message "PDF uploaded successfully."}))

          (catch Exception e
            (println "DB ERROR:" (.getMessage e))
            (-> (response/redirect (str "/project/" project-id))
                (assoc :flash {:error "DB insert failed"})))))

      (do
        (println "MISSING DATA")
        (-> (response/redirect (str "/project/" id))
            (assoc :flash {:error "Upload failed - missing data"}))))))
