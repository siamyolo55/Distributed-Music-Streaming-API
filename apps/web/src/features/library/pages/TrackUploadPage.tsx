import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { uploadTrack } from "../../../api";
import { useAuth } from "../../auth/AuthContext";

export function TrackUploadPage() {
  const { token, userId } = useAuth();
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [genre, setGenre] = useState("");
  const [artistName, setArtistName] = useState("");
  const [artistId, setArtistId] = useState(userId);
  const [file, setFile] = useState<File | null>(null);
  const [status, setStatus] = useState("Metadata + file input is mocked to a dummy media URL in backend for now.");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (!file) {
      setStatus("Select a file first.");
      return;
    }

    try {
      const response = await uploadTrack({ title, artistId, artistName, genre, file }, token);
      setStatus(JSON.stringify(response, null, 2));
      navigate("/tracks");
    } catch (error) {
      setStatus(String(error));
    }
  }

  return (
    <section className="page-section">
      <header className="section-header section-header-row">
        <div>
          <h2>Upload Track</h2>
          <p>Create a track entry with genre and artist metadata.</p>
        </div>
        <Link className="upload-link secondary-link" to="/tracks">
          Back To Tracks
        </Link>
      </header>

      <form className="panel form" onSubmit={onSubmit}>
        <input placeholder="Track name" value={title} onChange={(e) => setTitle(e.target.value)} />
        <input placeholder="Genre (e.g. Pop, Rock)" value={genre} onChange={(e) => setGenre(e.target.value)} />
        <input placeholder="Artist name (profile/display name)" value={artistName} onChange={(e) => setArtistName(e.target.value)} />
        <input placeholder="Artist ID" value={artistId} onChange={(e) => setArtistId(e.target.value)} />
        <input type="file" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
        <button type="submit">Save Track</button>
      </form>

      <pre className="small-log">{status}</pre>
    </section>
  );
}
