-- Place your queries here. Docs available [https://www.hugsql.org/](https://www.hugsql.org/)

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
) VALUES (
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
    updated_at = now()
WHERE id = :id;

-- :name create-project-file! :! :n
INSERT INTO project_files (
  project_id,
  original_filename,
  stored_filename,
  content_type,
  storage_path,
  file_size
) VALUES (
  :project_id,
  :original_filename,
  :stored_filename,
  :content_type,
  :storage_path,
  :file_size
);
