# DMSA Web

React + Vite frontend for the Distributed Music Streaming API.

## Structure

- `src/app`: routing, layout shell, global app styles
- `src/features/auth`: auth context + login page
- `src/features/home`: dashboard/home
- `src/features/library`: playlists/tracks pages
- `src/features/social`: following page
- `src/features/profile`: profile page

## Run locally

```bash
npm install
npm run dev
```

Default URL: `http://localhost:5173`

## Environment

- `VITE_USER_API_BASE_URL` (default `http://localhost:8081`)
- `VITE_MEDIA_API_BASE_URL` (default `http://localhost:8082`)

## Docker

The root `docker-compose.yml` serves this app at `http://localhost:8080`.
