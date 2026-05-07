
-- =========================================================
-- 🧙 PROJECTS
-- Core project management queries
-- =========================================================

-- :name get-projects :? :*
SELECT *
FROM projects
ORDER BY list, state, priority, updated_at DESC;

-- :name get-projects-by-list :? :*
SELECT *
FROM projects
WHERE list = :list
ORDER BY state, priority, updated_at DESC;

-- :name get-project :? :1
SELECT *
FROM projects
WHERE id = :id;

-- :name create-project! :! :n
INSERT INTO projects (
  title,
  description,
  is_personal,
  programming_lang,
  has_test_suite,
  is_open_source,
  hourly_rate_cents
)
VALUES (
  :title,
  :description,
  :is_personal,
  :programming_lang,
  :has_test_suite,
  :is_open_source,
  :hourly_rate_cents
);

-- :name update-project! :! :n
UPDATE projects
SET title = COALESCE(:title, title),
    description = COALESCE(:description, description),
    details = COALESCE(:details, details),
    is_personal = COALESCE(:is_personal, is_personal),
    programming_lang = COALESCE(:programming_lang, programming_lang),
    has_test_suite = COALESCE(:has_test_suite, has_test_suite),
    is_open_source = COALESCE(:is_open_source, is_open_source),
    list = COALESCE(:list, list),
    state = COALESCE(:state, state),
    priority = COALESCE(:priority, priority),
    hourly_rate_cents = COALESCE(:hourly_rate_cents, hourly_rate_cents),
    minutes_worked = COALESCE(:minutes_worked, minutes_worked),
    max_budget_cents = COALESCE(:max_budget_cents, max_budget_cents),
    updated_at = now()
WHERE id = :id;


-- =========================================================
-- 📜 PROJECT FILES (Codex / PDFs)
-- Uploaded documents tied to a project
-- =========================================================

-- :name create-project-file! :! :n
INSERT INTO project_files (
  project_id,
  original_filename,
  stored_filename,
  content_type,
  storage_path,
  file_size
)
VALUES (
  :project_id,
  :original_filename,
  :stored_filename,
  :content_type,
  :storage_path,
  :file_size
);

-- :name get-project-files :? :*
SELECT *
FROM project_files
WHERE project_id = :project_id
ORDER BY created_at DESC;


-- =========================================================
-- 💰 PAYMENTS / LEDGER
-- Financial tracking per project
-- =========================================================

-- :name create-payment! :! :n
INSERT INTO payments (
  project_id,
  invoice_number,
  original_amount_cents,
  original_currency,
  usd_amount_cents,
  note,
  paid_at
)
VALUES (
  :project_id,
  :invoice_number,
  :original_amount_cents,
  :original_currency,
  :usd_amount_cents,
  :note,
  COALESCE(:paid_at::date, CURRENT_DATE)
);

-- :name get-project-payments :? :*
SELECT *
FROM payments
WHERE project_id = :project_id
ORDER BY paid_at DESC, id DESC;

-- :name get-project-payments-total-usd :? :1
SELECT COALESCE(SUM(usd_amount_cents), 0) AS total_usd_cents
FROM payments
WHERE project_id = :project_id;

-- :name delete-payment! :! :n
DELETE FROM payments
WHERE id = :id
  AND project_id = :project_id;
