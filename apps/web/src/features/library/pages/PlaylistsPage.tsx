export function PlaylistsPage() {
  return (
    <section className="page-section">
      <header className="section-header">
        <h2>Playlists</h2>
        <p>Playlist list and editor view will live here. Next steps: wire FR-105 read/create/update/delete.</p>
      </header>

      <div className="tile-grid">
        <article className="tile">
          <h3>Your Playlists</h3>
          <p>Connect `/api/v1/users/me/playlists` list endpoint.</p>
        </article>
        <article className="tile">
          <h3>Playlist Detail</h3>
          <p>Add nested route for playlist tracks once `playlist_tracks` is implemented.</p>
        </article>
      </div>
    </section>
  );
}
