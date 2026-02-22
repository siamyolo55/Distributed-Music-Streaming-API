import { FormEvent, useMemo, useState } from "react";
import { follow, listFollows, login, mediaUrl, oauthLogin, register, unfollow, uploadTrack } from "./api";

function App() {
  const [log, setLog] = useState<string>("Ready.");
  const [token, setToken] = useState<string>("");
  const [audioUrl, setAudioUrl] = useState<string>("");

  const [regEmail, setRegEmail] = useState("");
  const [regPassword, setRegPassword] = useState("pass123");
  const [regDisplayName, setRegDisplayName] = useState("");

  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("pass123");

  const [oauthProvider, setOauthProvider] = useState("google");
  const [oauthProviderUserId, setOauthProviderUserId] = useState("");
  const [oauthEmail, setOauthEmail] = useState("");
  const [oauthDisplayName, setOauthDisplayName] = useState("");

  const [targetUserId, setTargetUserId] = useState("");
  const [trackTitle, setTrackTitle] = useState("");
  const [artistId, setArtistId] = useState("");
  const [trackFile, setTrackFile] = useState<File | null>(null);

  const tokenPreview = useMemo(() => (token ? `${token.slice(0, 70)}...` : "No token"), [token]);

  const writeLog = (label: string, payload: unknown) => {
    const serialized = typeof payload === "string" ? payload : JSON.stringify(payload, null, 2);
    setLog(`[${new Date().toISOString()}] ${label}\n${serialized}`);
  };

  async function run<T>(label: string, fn: () => Promise<T>) {
    try {
      const result = await fn();
      writeLog(`${label} OK`, result);
      return result;
    } catch (error) {
      writeLog(`${label} FAILED`, String(error));
      return null;
    }
  }

  async function onRegister(event: FormEvent) {
    event.preventDefault();
    const result = await run("REGISTER", () =>
      register({ email: regEmail, password: regPassword, displayName: regDisplayName })
    );
    if (result && typeof result === "object" && "userId" in result) {
      setArtistId(String(result.userId));
    }
  }

  async function onLogin(event: FormEvent) {
    event.preventDefault();
    const result = await run("LOGIN", () => login({ email: loginEmail, password: loginPassword }));
    if (result?.accessToken) {
      setToken(result.accessToken);
    }
  }

  async function onOAuthLogin(event: FormEvent) {
    event.preventDefault();
    const result = await run("OAUTH LOGIN", () =>
      oauthLogin({
        provider: oauthProvider,
        providerUserId: oauthProviderUserId,
        email: oauthEmail,
        displayName: oauthDisplayName
      })
    );
    if (result?.accessToken) {
      setToken(result.accessToken);
      setArtistId(result.userId);
    }
  }

  async function onUpload(event: FormEvent) {
    event.preventDefault();
    if (!trackFile) {
      writeLog("UPLOAD FAILED", "Select an audio file first.");
      return;
    }
    const result = await run("UPLOAD", () =>
      uploadTrack({ title: trackTitle, artistId, file: trackFile }, token)
    );
    if (result?.payload?.storagePath) {
      setAudioUrl(mediaUrl(result.payload.storagePath));
    }
  }

  return (
    <div className="page">
      <header className="hero">
        <h1>Distributed Music Streaming</h1>
        <p>New React frontend (`apps/web`) for register, auth, follow, and upload testing.</p>
      </header>

      <main className="grid">
        <section className="card">
          <h2>Register</h2>
          <form onSubmit={onRegister}>
            <input placeholder="Email" value={regEmail} onChange={(e) => setRegEmail(e.target.value)} />
            <input placeholder="Password" type="password" value={regPassword} onChange={(e) => setRegPassword(e.target.value)} />
            <input placeholder="Display Name" value={regDisplayName} onChange={(e) => setRegDisplayName(e.target.value)} />
            <button type="submit">Register</button>
          </form>
        </section>

        <section className="card">
          <h2>Login</h2>
          <form onSubmit={onLogin}>
            <input placeholder="Email" value={loginEmail} onChange={(e) => setLoginEmail(e.target.value)} />
            <input placeholder="Password" type="password" value={loginPassword} onChange={(e) => setLoginPassword(e.target.value)} />
            <button type="submit">Login</button>
          </form>
          <div className="token">{tokenPreview}</div>
        </section>

        <section className="card">
          <h2>OAuth (Baseline)</h2>
          <form onSubmit={onOAuthLogin}>
            <select value={oauthProvider} onChange={(e) => setOauthProvider(e.target.value)}>
              <option value="google">google</option>
              <option value="github">github</option>
              <option value="apple">apple</option>
              <option value="spotify">spotify</option>
            </select>
            <input placeholder="Provider User ID" value={oauthProviderUserId} onChange={(e) => setOauthProviderUserId(e.target.value)} />
            <input placeholder="Email" value={oauthEmail} onChange={(e) => setOauthEmail(e.target.value)} />
            <input placeholder="Display Name" value={oauthDisplayName} onChange={(e) => setOauthDisplayName(e.target.value)} />
            <button type="submit">OAuth Login</button>
          </form>
        </section>

        <section className="card">
          <h2>Follow Management</h2>
          <input placeholder="Target User ID" value={targetUserId} onChange={(e) => setTargetUserId(e.target.value)} />
          <div className="actions">
            <button onClick={() => run("FOLLOW", () => follow(targetUserId, token))}>Follow</button>
            <button onClick={() => run("UNFOLLOW", () => unfollow(targetUserId, token))}>Unfollow</button>
            <button onClick={() => run("LIST FOLLOWS", () => listFollows(token))}>List</button>
          </div>
        </section>

        <section className="card">
          <h2>Upload Track</h2>
          <form onSubmit={onUpload}>
            <input placeholder="Track Title" value={trackTitle} onChange={(e) => setTrackTitle(e.target.value)} />
            <input placeholder="Artist/User ID" value={artistId} onChange={(e) => setArtistId(e.target.value)} />
            <input type="file" accept="audio/*" onChange={(e) => setTrackFile(e.target.files?.[0] ?? null)} />
            <button type="submit">Upload</button>
          </form>
          {audioUrl ? <audio controls src={audioUrl} /> : null}
        </section>
      </main>

      <pre className="log">{log}</pre>
    </div>
  );
}

export default App;
