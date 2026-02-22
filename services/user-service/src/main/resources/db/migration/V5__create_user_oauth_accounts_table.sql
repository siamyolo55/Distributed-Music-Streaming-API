CREATE TABLE IF NOT EXISTS user_oauth_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(320),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_user_oauth_provider_subject UNIQUE (provider, provider_user_id)
);

CREATE INDEX IF NOT EXISTS idx_user_oauth_accounts_user_id ON user_oauth_accounts(user_id);
