package com.musicstreaming.userservice.playlist.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, UUID> {
    List<PlaylistTrack> findAllByPlaylistIdOrderByPositionAsc(UUID playlistId);

    List<PlaylistTrack> findAllByPlaylistIdInOrderByPlaylistIdAscPositionAsc(Collection<UUID> playlistIds);

    void deleteByPlaylistId(UUID playlistId);
}
