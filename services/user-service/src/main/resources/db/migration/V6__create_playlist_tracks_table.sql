CREATE TABLE IF NOT EXISTS playlist_tracks (
    id UUID PRIMARY KEY,
    playlist_id UUID NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    track_id VARCHAR(64) NOT NULL,
    title VARCHAR(200) NOT NULL,
    artist_name VARCHAR(200) NOT NULL,
    genre VARCHAR(80) NOT NULL,
    position INTEGER NOT NULL,
    added_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_playlist_tracks_unique_track UNIQUE (playlist_id, track_id)
);

CREATE INDEX IF NOT EXISTS idx_playlist_tracks_playlist_id ON playlist_tracks(playlist_id);
CREATE INDEX IF NOT EXISTS idx_playlist_tracks_playlist_position ON playlist_tracks(playlist_id, position);
