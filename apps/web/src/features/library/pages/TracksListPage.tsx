import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listTracks, type TrackItem } from "../../../api";
import { useAuth } from "../../auth/AuthContext";

export function TracksListPage() {
  const { token } = useAuth();
  const [tracks, setTracks] = useState<TrackItem[]>([]);
  const [status, setStatus] = useState("Loading tracks...");

  async function loadTracks() {
    try {
      const items = await listTracks(token);
      setTracks(items);
      setStatus(`Loaded ${items.length} track(s).`);
    } catch (error) {
      setStatus(String(error));
    }
  }

  useEffect(() => {
    void loadTracks();
  }, []);

  return (
    <section className="page-section">
      <header className="section-header section-header-row">
        <div>
          <h2>Tracks</h2>
          <p>List of uploaded tracks with metadata.</p>
        </div>
        <Link className="upload-link" to="/tracks/upload">
          Upload Track
        </Link>
      </header>

      <div className="panel">
        <button className="secondary" onClick={() => void loadTracks()}>
          Refresh
        </button>
      </div>

      <div className="table-wrap">
        <table className="tracks-table">
          <thead>
            <tr>
              <th>Track</th>
              <th>Artist</th>
              <th>Genre</th>
              <th>File URL</th>
              <th>Uploaded</th>
            </tr>
          </thead>
          <tbody>
            {tracks.map((track) => (
              <tr key={track.trackId}>
                <td>{track.title}</td>
                <td>{track.artistName}</td>
                <td>{track.genre}</td>
                <td>
                  <a href={track.fileUrl} target="_blank" rel="noreferrer">
                    Open
                  </a>
                </td>
                <td>{new Date(track.createdAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <pre className="small-log">{status}</pre>
    </section>
  );
}
