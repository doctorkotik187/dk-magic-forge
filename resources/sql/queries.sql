-- Place your queries here. Docs available [https://www.hugsql.org/](https://www.hugsql.org/)

-- :name get-projects :? :*
SELECT * FROM projects ORDER BY list, state, priority, updated_at DESC;

-- :name get-project :? :1
SELECT * FROM projects WHERE id = :id;

-- :name create-project! :! :n
INSERT INTO projects
(title, description, programming_lang, client_budget_cents, is_open_source)
VALUES (:title, :description, :programming_lang, :client_budget_cents, :is_open_source);

-- :name update-state! :! :n
UPDATE projects
SET state = :state, updated_at = now()
WHERE id = :id;

-- :name update-list! :! :n
UPDATE projects
SET list = :list, updated_at = now()
WHERE id = :id;
