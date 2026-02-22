const cards = [
  { title: "For You", subtitle: "Recommendations API to be wired" },
  { title: "Trending", subtitle: "Analytics + recommendation window" },
  { title: "Recently Played", subtitle: "Playback events pipeline" }
];

export function HomePage() {
  return (
    <section className="page-section">
      <header className="section-header">
        <h2>Home</h2>
        <p>Spotify-style dashboard shell. Connect FR-401..FR-505 data here later.</p>
      </header>

      <div className="tile-grid">
        {cards.map((card) => (
          <article key={card.title} className="tile">
            <h3>{card.title}</h3>
            <p>{card.subtitle}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
