CREATE TABLE projects (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- user_id INTEGER NOT NULL REFERENCES users(id),

    title VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    details TEXT,
    is_personal BOOLEAN DEFAULT false,

    -- Tech
    programming_lang VARCHAR(50) DEFAULT 'clojure',
    has_test_suite BOOLEAN DEFAULT false,
    is_open_source BOOLEAN DEFAULT false,

    -- GTD productivity
    list VARCHAR(20) NOT NULL DEFAULT 'inbox',
    state VARCHAR(20) DEFAULT 'noop',
    priority VARCHAR(1) DEFAULT 'b',

    -- finance
    hourly_rate_cents INTEGER DEFAULT 0,
    minutes_worked INTEGER DEFAULT 0,

    -- lifecycle timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
