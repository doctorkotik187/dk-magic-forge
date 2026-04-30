CREATE TABLE projects (
    id UUID PRIMARY KEY,

    title VARCHAR(120) NOT NULL,
    description TEXT,

    -- GTD / workflow layer
    list VARCHAR(20) NOT NULL DEFAULT 'inbox',
    state VARCHAR(20) DEFAULT 'none',

    -- ordering (optional but useful)
    priority INTEGER DEFAULT 100,

    -- client-facing intake signal (NOT your pricing)
    client_budget_cents INTEGER,

    -- lifecycle timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
