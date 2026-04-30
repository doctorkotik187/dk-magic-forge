-- Place your queries here. Docs available https://www.hugsql.org/

-- :name get-projects :? :*
SELECT * FROM projects
ORDER BY list, state, created_at DESC;

-- :name create-project! :! :n
INSERT INTO projects
(id, title, description, list, state, client_budget_cents)
VALUES (:id, :title, :description, :list, :state, :client_budget_cents);

-- :name update-state! :! :n
UPDATE projects
SET state = :state,
    updated_at = now()
WHERE id = :id;

-- :name update-list! :! :n
UPDATE projects
SET list = :list,
    updated_at = now()
WHERE id = :id;
