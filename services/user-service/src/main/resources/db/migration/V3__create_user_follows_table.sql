CREATE TABLE IF NOT EXISTS user_follows (
    id UUID PRIMARY KEY,
    follower_user_id UUID NOT NULL,
    target_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_user_follows_pair UNIQUE (follower_user_id, target_user_id),
    CONSTRAINT fk_user_follows_follower FOREIGN KEY (follower_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_follows_target FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_user_follows_not_self CHECK (follower_user_id <> target_user_id)
);

CREATE INDEX IF NOT EXISTS idx_user_follows_follower_user_id ON user_follows(follower_user_id);
CREATE INDEX IF NOT EXISTS idx_user_follows_target_user_id ON user_follows(target_user_id);

INSERT INTO user_follows (id, follower_user_id, target_user_id, created_at)
SELECT af.id, af.user_id, CAST(af.artist_id AS UUID), af.created_at
FROM artist_follows af
JOIN users u ON u.id = CAST(af.artist_id AS UUID)
WHERE af.artist_id ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$'
  AND af.user_id <> CAST(af.artist_id AS UUID)
ON CONFLICT (follower_user_id, target_user_id) DO NOTHING;
