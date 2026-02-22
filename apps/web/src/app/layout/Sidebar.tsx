import { NavLink } from "react-router-dom";
import { useAuth } from "../../features/auth/AuthContext";

const items = [
  { to: "/", label: "Home" },
  { to: "/playlists", label: "Playlists" },
  { to: "/tracks", label: "Tracks" },
  { to: "/following", label: "Following" },
  { to: "/profile", label: "Profile" }
];

export function Sidebar() {
  const { clearToken } = useAuth();

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">MS</div>
        <div>
          <h1>Music Stream</h1>
          <p>Web App</p>
        </div>
      </div>

      <nav className="nav">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/"}
            className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>

      <button className="ghost-btn" onClick={clearToken}>
        Sign out
      </button>
    </aside>
  );
}
