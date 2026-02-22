import { FormEvent, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { login, oauthLogin, register } from "../../../api";
import { useAuth } from "../AuthContext";

export function LoginPage() {
  const { setToken } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = (location.state as { from?: string } | undefined)?.from ?? "/";

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("pass123");
  const [displayName, setDisplayName] = useState("");
  const [provider, setProvider] = useState("google");
  const [providerUserId, setProviderUserId] = useState("");
  const [authMode, setAuthMode] = useState<"login" | "register">("login");
  const [message, setMessage] = useState("Use email/password or OAuth baseline login.");

  const buttonLabel = useMemo(() => (authMode === "login" ? "Sign in" : "Create account"), [authMode]);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    try {
      if (authMode === "login") {
        const response = await login({ email, password });
        setToken(response.accessToken);
      } else {
        await register({ email, password, displayName });
        const response = await login({ email, password });
        setToken(response.accessToken);
      }
      navigate(redirectTo, { replace: true });
    } catch (error) {
      setMessage(String(error));
    }
  }

  async function onOauthSubmit(event: FormEvent) {
    event.preventDefault();
    try {
      const response = await oauthLogin({
        provider,
        providerUserId,
        email,
        displayName: displayName || email.split("@")[0] || "music-user"
      });
      setToken(response.accessToken);
      navigate(redirectTo, { replace: true });
    } catch (error) {
      setMessage(String(error));
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-panel">
        <h1>Music Stream</h1>
        <p>Build your Spotify-style app on top of your microservices backend.</p>

        <div className="tab-row">
          <button
            className={`tab ${authMode === "login" ? "tab-active" : ""}`}
            onClick={() => setAuthMode("login")}
          >
            Login
          </button>
          <button
            className={`tab ${authMode === "register" ? "tab-active" : ""}`}
            onClick={() => setAuthMode("register")}
          >
            Register
          </button>
        </div>

        <form onSubmit={onSubmit} className="form">
          <input placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} />
          <input placeholder="Password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          {authMode === "register" ? (
            <input
              placeholder="Display Name"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
            />
          ) : null}
          <button type="submit">{buttonLabel}</button>
        </form>

        <div className="divider">OAuth extension (local baseline)</div>

        <form onSubmit={onOauthSubmit} className="form">
          <select value={provider} onChange={(e) => setProvider(e.target.value)}>
            <option value="google">google</option>
            <option value="github">github</option>
            <option value="apple">apple</option>
            <option value="spotify">spotify</option>
          </select>
          <input
            placeholder="Provider User ID"
            value={providerUserId}
            onChange={(e) => setProviderUserId(e.target.value)}
          />
          <button type="submit">Continue with OAuth</button>
        </form>

        <pre className="small-log">{message}</pre>
      </div>
    </div>
  );
}
