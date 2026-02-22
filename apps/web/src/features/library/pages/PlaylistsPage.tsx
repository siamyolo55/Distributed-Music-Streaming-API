import { useEffect, useMemo, useState, type FormEvent } from "react";
import { createPlaylist, listPlaylists, listTracks, type PlaylistItem, type TrackItem } from "../../../api";
import { useAuth } from "../../auth/AuthContext";

export function PlaylistsPage() {
  const { token } = useAuth();
  const [tracks, setTracks] = useState<TrackItem[]>([]);
  const [playlists, setPlaylists] = useState<PlaylistItem[]>([]);
  const [selectedTrackIds, setSelectedTrackIds] = useState<string[]>([]);
  const [name, setName] = useState("");
  const [vibes, setVibes] = useState("");
  const [status, setStatus] = useState("Loading playlists and tracks...");

  const selectedTracks = useMemo(
    () => tracks.filter((track) => selectedTrackIds.includes(track.trackId)),
    [tracks, selectedTrackIds]
  );

  async function loadData() {
    try {
      const [trackItems, playlistItems] = await Promise.all([listTracks(token), listPlaylists(token)]);
      setTracks(trackItems);
      setPlaylists(playlistItems);
      setStatus(`Loaded ${trackItems.length} track(s), ${playlistItems.length} playlist(s).`);
    } catch (error) {
      setStatus(String(error));
    }
  }

  useEffect(() => {
    void loadData();
  }, []);

  function toggleTrack(trackId: string) {
    setSelectedTrackIds((current) =>
      current.includes(trackId) ? current.filter((id) => id !== trackId) : [...current, trackId]
    );
  }

  async function onCreatePlaylist(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (selectedTracks.length === 0) {
      setStatus("Select at least one track before creating a playlist.");
      return;
    }

    try {
      const created = await createPlaylist(
        {
          name,
          description: vibes.trim(),
          tracks: selectedTracks.map((track) => ({
            trackId: track.trackId,
            title: track.title,
            artistName: track.artistName,
            genre: track.genre
          }))
        },
        token
      );
      setName("");
      setVibes("");
      setSelectedTrackIds([]);
      setPlaylists((current) => [created, ...current]);
      setStatus(`Created playlist "${created.name}" with ${created.tracks.length} track(s).`);
    } catch (error) {
      setStatus(String(error));
    }
  }

  return (
    <section className="page-section">
      <header className="section-header">
        <h2>Playlists</h2>
        <p>Create playlists by selecting tracks and adding the playlist vibe.</p>
      </header>

      <form className="form panel" onSubmit={(event) => void onCreatePlaylist(event)}>
        <input
          value={name}
          onChange={(event) => setName(event.target.value)}
          placeholder="Playlist name"
          maxLength={120}
          required
        />
        <input
          value={vibes}
          onChange={(event) => setVibes(event.target.value)}
          placeholder="Vibes (optional)"
          maxLength={600}
        />
        <button type="submit">Create Playlist With Selected Tracks</button>
      </form>

      <div className="table-wrap">
        <table className="tracks-table">
          <thead>
            <tr>
              <th>Select</th>
              <th>Track</th>
              <th>Artist</th>
              <th>Genre</th>
            </tr>
          </thead>
          <tbody>
            {tracks.map((track) => (
              <tr key={track.trackId}>
                <td>
                  <input
                    className="row-checkbox"
                    type="checkbox"
                    checked={selectedTrackIds.includes(track.trackId)}
                    onChange={() => toggleTrack(track.trackId)}
                  />
                </td>
                <td>{track.title}</td>
                <td>{track.artistName}</td>
                <td>{track.genre}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <section className="panel">
        <h3>Your Playlists</h3>
        <div className="playlist-grid">
          {playlists.map((playlist) => (
            <article className="tile" key={playlist.id}>
              <h3>{playlist.name}</h3>
              <p>{playlist.description || "No vibe description."}</p>
              <p>{playlist.tracks.length} track(s)</p>
              <div className="chip-row">
                {playlist.tracks.slice(0, 6).map((track) => (
                  <span className="chip" key={`${playlist.id}-${track.trackId}`}>
                    {track.title}
                  </span>
                ))}
              </div>
            </article>
          ))}
        </div>
      </section>

      <pre className="small-log">{status}</pre>
    </section>
  );
}
