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

(defn format-date [ts]
  (when ts
    (subs (str ts) 0 10)))

(defn cents->dollars [cents]
  (when cents
    (/ cents 100.0)))

(defn minutes->hours [minutes]
  (when minutes
    (/ minutes 60.0)))

(defn dwarf-group [state]
  (case (str/lower-case (or state ""))
    ("todo" "doing") "active"
    "waiting" "waiting"
    "idle"))

;; ------------------------------------------------------------
;; core view model
;; ------------------------------------------------------------

(defn enrich-project [p]
  (-> p
      ;; derived UI fields
      (assoc :dwarf_group (dwarf-group (:state p)))
      (assoc :dwarf (inc (rand-int 8)))

      ;; time formatting
      (update :created_at format-date)
      (update :updated_at format-date)

      ;; finance + time conversion
      (assoc :hours_worked (minutes->hours (:minutes_worked p)))
      (assoc :hourly_rate (cents->dollars (:hourly_rate_cents p)))))

;; ------------------------------------------------------------
;; list views
;; ------------------------------------------------------------

(defn list-projects
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "projects"})
                      (map enrich-project))]
    (layout/render request "projects.html"
                   {:projects projects
                    :valid_lists valid-lists
                    :valid_states valid-states
                    :valid_priorities valid-priorities
                    :flash (:flash request)})))

(defn list-inbox
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "inbox"})
                      (map enrich-project))]
    (layout/render request "inbox.html"
                   {:projects projects
                    :valid_lists valid-lists
                    :valid_states valid-states
                    :valid_priorities valid-priorities
                    :flash (:flash request)})))

(defn list-someday
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "someday"})
                      (map enrich-project))]
    (layout/render request "someday.html"
                   {:projects projects
                    :valid_lists valid-lists
                    :valid_states valid-states
                    :valid_priorities valid-priorities
                    :flash (:flash request)})))

(defn list-archives
  [{:keys [query-fn]} request]
  (let [projects (->> (query-fn :get-projects-by-list {:list "archives"})
                      (map enrich-project))]
    (layout/render request "archives.html"
                   {:projects projects
                    :valid_lists valid-lists
                    :valid_states valid-states
                    :valid_priorities valid-priorities
                    :flash (:flash request)})))

;; ------------------------------------------------------------
;; single project
;; ------------------------------------------------------------

(defn show
  [{:keys [query-fn]}
   {{:keys [id]} :path-params :as request}]
  (if-let [project-id (and id (try (parse-long id) (catch Exception _ nil)))]
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
  (if-let [project-id (and id (try (parse-long id) (catch Exception _ nil)))]
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

        hourly-rate (some-> hourly_rate parse-long)
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

(defn present-keys [m]
  (into {} (remove (comp nil? val) m)))

(defn update!
  [{:keys [query-fn]}
   {{:keys [id]} :path-params
    params :form-params}]
  (let [id (parse-long id)
        existing (query-fn :project-by-id {:id id})
        patch (-> params
                  (update "hourly_rate" some-> parse-long)
                  (update "hours_worked" some-> parse-long)
                  present-keys)
        updated (merge existing patch)]
    (query-fn :update-project! updated)))
