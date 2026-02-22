CREATE TABLE IF NOT EXISTS tracks (
    track_id VARCHAR(40) PRIMARY KEY,
    artist_id VARCHAR(80) NOT NULL,
    artist_name VARCHAR(160) NOT NULL,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(80) NOT NULL,
    file_url VARCHAR(1024) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tracks_artist_id ON tracks(artist_id);
CREATE INDEX IF NOT EXISTS idx_tracks_created_at_desc ON tracks(created_at DESC);
