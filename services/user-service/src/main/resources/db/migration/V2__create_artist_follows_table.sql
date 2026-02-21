CREATE TABLE IF NOT EXISTS artist_follows (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    artist_id VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_artist_follows_user_artist UNIQUE (user_id, artist_id),
    CONSTRAINT fk_artist_follows_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_artist_follows_user_id ON artist_follows(user_id);
