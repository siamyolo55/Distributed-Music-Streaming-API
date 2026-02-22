export type JsonRecord = Record<string, unknown>;
export type TrackItem = {
  trackId: string;
  artistId: string;
  artistName: string;
  title: string;
  genre: string;
  fileUrl: string;
  createdAt: string;
};
export type PlaylistTrackItem = {
  trackId: string;
  title: string;
  artistName: string;
  genre: string;
  position: number;
};
export type PlaylistItem = {
  id: string;
  name: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
  tracks: PlaylistTrackItem[];
};
export type DiscoverUserItem = {
  userId: string;
  displayName: string;
  email: string;
  createdAt: string;
};
export type FollowedUserItem = {
  targetUserId: string;
  followedAt: string;
};

const defaultApiBase = window.location.origin;
const userApi = import.meta.env.VITE_USER_API_BASE_URL ?? defaultApiBase;
const mediaApi = import.meta.env.VITE_MEDIA_API_BASE_URL ?? defaultApiBase;

async function request<T>(
  url: string,
  init?: RequestInit,
  token?: string
): Promise<T> {
  const headers = new Headers(init?.headers);
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  const response = await fetch(url, { ...init, headers });
  const text = await response.text();
  let body: unknown = {};
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = text;
    }
  }
  if (!response.ok) {
    const details = typeof body === "string" ? body : JSON.stringify(body);
    throw new Error(`HTTP ${response.status}: ${details}`);
  }
  return body as T;
}

export function register(payload: { email: string; password: string; displayName: string }) {
  return request(`${userApi}/api/v1/public/users/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export function login(payload: { email: string; password: string }) {
  return request<{ accessToken: string; tokenType: string }>(`${userApi}/api/v1/public/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export function oauthLogin(payload: {
  provider: string;
  providerUserId: string;
  email: string;
  displayName: string;
}) {
  return request<{
    accessToken: string;
    tokenType: string;
    userId: string;
    status: string;
  }>(`${userApi}/api/v1/public/auth/oauth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export function follow(targetUserId: string, token: string) {
  return request(`${userApi}/api/v1/users/me/follows/${encodeURIComponent(targetUserId)}`, {
    method: "POST"
  }, token);
}

export function unfollow(targetUserId: string, token: string) {
  return request(`${userApi}/api/v1/users/me/follows/${encodeURIComponent(targetUserId)}`, {
    method: "DELETE"
  }, token);
}

export function listFollows(token: string) {
  return request<FollowedUserItem[]>(`${userApi}/api/v1/users/me/follows`, { method: "GET" }, token);
}

export function listDiscoverUsers(token: string) {
  return request<DiscoverUserItem[]>(`${userApi}/api/v1/users/discover`, { method: "GET" }, token);
}

export async function uploadTrack(
  payload: { title: string; artistId: string; artistName: string; genre: string; file: File },
  token: string
) {
  const form = new FormData();
  form.append("title", payload.title);
  form.append("artistId", payload.artistId);
  form.append("artistName", payload.artistName);
  form.append("genre", payload.genre);
  form.append("file", payload.file);
  return request<{ payload?: { storagePath?: string; trackId?: string } }>(
    `${mediaApi}/api/v1/media/tracks`,
    { method: "POST", body: form },
    token
  );
}

export function listTracks(token: string) {
  return request<TrackItem[]>(`${mediaApi}/api/v1/media/tracks`, { method: "GET" }, token);
}

export function mediaUrl(path: string) {
  return `${mediaApi}${path}`;
}

export function createPlaylist(
  payload: {
    name: string;
    description?: string;
    tracks: Array<{ trackId: string; title: string; artistName: string; genre: string }>;
  },
  token: string
) {
  return request<PlaylistItem>(`${userApi}/api/v1/users/me/playlists`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  }, token);
}

export function listPlaylists(token: string) {
  return request<PlaylistItem[]>(`${userApi}/api/v1/users/me/playlists`, { method: "GET" }, token);
}
