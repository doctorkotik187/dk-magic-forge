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
SET title = :title,
    description = :description,
    details = :details,
    is_personal = :is_personal,
    programming_lang = :programming_lang,
    has_test_suite = :has_test_suite,
    is_open_source = :is_open_source,
    list = :list,
    state = :state,
    priority = :priority,
    hourly_rate_cents = :hourly_rate_cents,
    minutes_worked = :minutes_worked,
    updated_at = now()
WHERE id = :id;
