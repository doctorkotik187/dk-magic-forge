-- Place your queries here. Docs available https://www.hugsql.org/

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
SELECT * FROM projects WHERE id = :id;

-- :name create-project! :! :n
INSERT INTO projects(
title, description, is_personal,
programming_lang, has_test_suite, is_open_source,
hourly_rate_cents
) VALUES (
:title, :description, :is_personal,
:programming_lang, :has_test_suite, :is_open_source,
:hourly_rate_cents
);

-- :name update-project! :! :n
UPDATE projects
SET list = :list,
    state = :state,
    updated_at = now()
WHERE id = :id;
