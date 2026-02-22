import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./layout/AppShell";
import { ProtectedRoute } from "./ProtectedRoute";
import { LoginPage } from "../features/auth/pages/LoginPage";
import { HomePage } from "../features/home/pages/HomePage";
import { PlaylistsPage } from "../features/library/pages/PlaylistsPage";
import { TracksListPage } from "../features/library/pages/TracksListPage";
import { TrackUploadPage } from "../features/library/pages/TrackUploadPage";
import { FollowingPage } from "../features/social/pages/FollowingPage";
import { ProfilePage } from "../features/profile/pages/ProfilePage";

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <AppShell />
            </ProtectedRoute>
          }
        >
          <Route index element={<HomePage />} />
          <Route path="playlists" element={<PlaylistsPage />} />
          <Route path="tracks" element={<TracksListPage />} />
          <Route path="tracks/upload" element={<TrackUploadPage />} />
          <Route path="following" element={<FollowingPage />} />
          <Route path="profile" element={<ProfilePage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
