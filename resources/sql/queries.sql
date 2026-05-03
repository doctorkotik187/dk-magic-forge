-- Place your queries here. Docs available https://www.hugsql.org/

-- =========================
-- ALL PROJECTS (admin view)
-- =========================
-- :name get-projects :? :*
SELECT *
FROM projects
ORDER BY list, state, priority, updated_at DESC;

-- =========================
-- PROJECTS BY LIST (MAIN FLOW)
-- =========================
-- :name get-projects-by-list :? :*
SELECT *
FROM projects
WHERE list = :list
ORDER BY state, priority, updated_at DESC;

-- =========================
-- SINGLE PROJECT
-- =========================
-- :name get-project :? :1
SELECT * FROM projects WHERE id = :id;

-- =========================
-- CREATE PROJECT
-- =========================
-- NOTE: list/state default handled in controller or DB defaults
-- =========================
-- :name create-project! :! :n
INSERT INTO projects
(title, description, programming_lang, client_budget_cents, is_open_source)
VALUES (:title, :description, :programming_lang, :client_budget_cents, :is_open_source);

-- =========================
-- STATE UPDATE
-- =========================
-- :name update-state! :! :n
UPDATE projects
SET state = :state,
    updated_at = now()
WHERE id = :id;

-- =========================
-- LIST UPDATE
-- =========================
-- :name update-list! :! :n
UPDATE projects
SET list = :list,
    updated_at = now()
WHERE id = :id;
