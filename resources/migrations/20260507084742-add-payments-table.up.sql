CREATE TABLE payments (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    project_id INTEGER NOT NULL
        REFERENCES projects(id)
        ON DELETE CASCADE,

    invoice_number VARCHAR(50),

    original_amount_cents INTEGER NOT NULL,
    original_currency CHAR(3) NOT NULL,

    usd_amount_cents INTEGER NOT NULL,

    note TEXT DEFAULT '',

    paid_at DATE DEFAULT CURRENT_DATE
);
