import { FormEvent, useState } from "react";
import { uploadTrack, mediaUrl } from "../../../api";
import { useAuth } from "../../auth/AuthContext";

export function TracksPage() {
  const { token, userId } = useAuth();
  const [title, setTitle] = useState("");
  const [artistId, setArtistId] = useState(userId);
  const [file, setFile] = useState<File | null>(null);
  const [audioSrc, setAudioSrc] = useState("");
  const [status, setStatus] = useState("Upload a track to local media storage.");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (!file) {
      setStatus("Select an audio file.");
      return;
    }
    try {
      const response = await uploadTrack({ title, artistId, file }, token);
      const path = response.payload?.storagePath ?? "";
      if (path) {
        setAudioSrc(mediaUrl(path));
      }
      setStatus(JSON.stringify(response, null, 2));
    } catch (error) {
      setStatus(String(error));
    }
  }

  return (
    <section className="page-section">
      <header className="section-header">
        <h2>Tracks</h2>
        <p>Track management and upload flow.</p>
      </header>

      <form className="panel form" onSubmit={onSubmit}>
        <input placeholder="Track title" value={title} onChange={(e) => setTitle(e.target.value)} />
        <input placeholder="Artist/User ID" value={artistId} onChange={(e) => setArtistId(e.target.value)} />
        <input type="file" accept="audio/*" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
        <button type="submit">Upload Track</button>
      </form>

      {audioSrc ? <audio controls src={audioSrc} className="audio" /> : null}

      <pre className="small-log">{status}</pre>
    </section>
  );
}
