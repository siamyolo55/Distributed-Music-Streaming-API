import { useEffect, useMemo, useState } from "react";
import {
  follow,
  listDiscoverUsers,
  listFollows,
  unfollow,
  type DiscoverUserItem
} from "../../../api";
import { useAuth } from "../../auth/AuthContext";

export function FollowingPage() {
  const { token } = useAuth();
  const [users, setUsers] = useState<DiscoverUserItem[]>([]);
  const [followedUserIds, setFollowedUserIds] = useState<Set<string>>(new Set());
  const [status, setStatus] = useState("Loading users...");

  const rows = useMemo(
    () =>
      users.map((user) => ({
        ...user,
        isFollowing: followedUserIds.has(user.userId)
      })),
    [users, followedUserIds]
  );

  async function loadData() {
    try {
      const [discoverableUsers, followedUsers] = await Promise.all([
        listDiscoverUsers(token),
        listFollows(token)
      ]);
      setUsers(discoverableUsers);
      setFollowedUserIds(new Set(followedUsers.map((item) => item.targetUserId)));
      setStatus(`Loaded ${discoverableUsers.length} discoverable user(s).`);
    } catch (error) {
      setStatus(String(error));
    }
  }

  useEffect(() => {
    void loadData();
  }, []);

  async function onFollow(targetUserId: string) {
    try {
      await follow(targetUserId, token);
      setFollowedUserIds((current) => {
        const next = new Set(current);
        next.add(targetUserId);
        return next;
      });
      setStatus("User followed.");
    } catch (error) {
      setStatus(String(error));
    }
  }

  async function onUnfollow(targetUserId: string) {
    try {
      await unfollow(targetUserId, token);
      setFollowedUserIds((current) => {
        const next = new Set(current);
        next.delete(targetUserId);
        return next;
      });
      setStatus("User unfollowed.");
    } catch (error) {
      setStatus(String(error));
    }
  }

  return (
    <section className="page-section">
      <header className="section-header">
        <h2>Following</h2>
        <p>Find users and follow/unfollow them from one place.</p>
      </header>

      <div className="panel">
        <button className="secondary" onClick={() => void loadData()}>Refresh Users</button>
      </div>

      <div className="table-wrap">
        <table className="tracks-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Joined</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((user) => (
              <tr key={user.userId}>
                <td>{user.displayName}</td>
                <td>{user.email}</td>
                <td>{new Date(user.createdAt).toLocaleString()}</td>
                <td>
                  {user.isFollowing ? (
                    <button className="secondary" onClick={() => void onUnfollow(user.userId)}>
                      Unfollow
                    </button>
                  ) : (
                    <button onClick={() => void onFollow(user.userId)}>Follow</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <pre className="small-log">{status}</pre>
    </section>
  );
}
