CREATE TABLE projects (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    title VARCHAR(120) NOT NULL,
    description TEXT,
    programming_lang VARCHAR(50) DEFAULT 'clojure',
    is_open_source BOOLEAN DEFAULT false,

    -- GTD / workflow layer
    list VARCHAR(20) NOT NULL DEFAULT 'inbox',
    state VARCHAR(20) DEFAULT 'noop',

    -- A, B, C, D
    priority VARCHAR(1) DEFAULT 'b',

    -- client-facing intake signal (NOT your pricing)
    client_budget_cents INTEGER,

    -- lifecycle timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
