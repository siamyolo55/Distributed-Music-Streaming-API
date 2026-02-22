import { useState } from "react";
import { follow, listFollows, unfollow } from "../../../api";
import { useAuth } from "../../auth/AuthContext";

export function FollowingPage() {
  const { token } = useAuth();
  const [targetUserId, setTargetUserId] = useState("");
  const [status, setStatus] = useState("Manage follow graph here.");

  async function run(label: string, operation: () => Promise<unknown>) {
    try {
      const response = await operation();
      setStatus(`${label}\n${JSON.stringify(response, null, 2)}`);
    } catch (error) {
      setStatus(`${label} FAILED\n${String(error)}`);
    }
  }

  return (
    <section className="page-section">
      <header className="section-header">
        <h2>Following</h2>
        <p>User-to-user follow relationships.</p>
      </header>

      <div className="panel form">
        <input
          placeholder="Target user id"
          value={targetUserId}
          onChange={(e) => setTargetUserId(e.target.value)}
        />
        <div className="button-row">
          <button onClick={() => run("FOLLOW", () => follow(targetUserId, token))}>Follow</button>
          <button className="secondary" onClick={() => run("UNFOLLOW", () => unfollow(targetUserId, token))}>
            Unfollow
          </button>
          <button className="secondary" onClick={() => run("LIST", () => listFollows(token))}>
            List
          </button>
        </div>
      </div>

      <pre className="small-log">{status}</pre>
    </section>
  );
}
