-- src/main/resources/db/migration/V1__init.sql
CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL,
    balance_amount NUMERIC NOT NULL,
    balance_currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    from_account VARCHAR(50),
    to_account VARCHAR(50),
    amount NUMERIC NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
    );