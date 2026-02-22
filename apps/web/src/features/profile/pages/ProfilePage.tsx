import { useAuth } from "../../auth/AuthContext";

export function ProfilePage() {
  const { userId, token } = useAuth();

  return (
    <section className="page-section">
      <header className="section-header">
        <h2>Profile</h2>
        <p>Identity from JWT claims. Expand this page once FR-106 preferences is implemented.</p>
      </header>

      <div className="tile-grid">
        <article className="tile">
          <h3>User ID</h3>
          <p>{userId || "Not found in token"}</p>
        </article>
        <article className="tile">
          <h3>Auth Token</h3>
          <p>{token ? `${token.slice(0, 60)}...` : "Missing token"}</p>
        </article>
      </div>
    </section>
  );
}
