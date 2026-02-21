package com.musicstreaming.userservice.playlist.service;

import com.musicstreaming.userservice.playlist.domain.Playlist;
import com.musicstreaming.userservice.playlist.domain.PlaylistRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public PlaylistService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    @Transactional
    public PlaylistDetails create(UUID userId, String name, String description) {
        Instant now = Instant.now();
        Playlist saved = playlistRepository.save(new Playlist(userId, name.trim(), normalizeDescription(description), now, now));
        return toDetails(saved);
    }

    @Transactional(readOnly = true)
    public List<PlaylistDetails> list(UUID userId) {
        return playlistRepository.findAllByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toDetails)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaylistDetails get(UUID userId, UUID playlistId) {
        Playlist playlist = findOwnedPlaylist(userId, playlistId);
        return toDetails(playlist);
    }

    @Transactional
    public PlaylistDetails update(UUID userId, UUID playlistId, String name, String description) {
        Playlist playlist = findOwnedPlaylist(userId, playlistId);
        playlist.update(name.trim(), normalizeDescription(description), Instant.now());
        return toDetails(playlist);
    }

    @Transactional
    public void delete(UUID userId, UUID playlistId) {
        Playlist playlist = findOwnedPlaylist(userId, playlistId);
        playlistRepository.delete(playlist);
    }

    private Playlist findOwnedPlaylist(UUID userId, UUID playlistId) {
        return playlistRepository.findByIdAndUserId(playlistId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
    }

    private PlaylistDetails toDetails(Playlist playlist) {
        return new PlaylistDetails(
                playlist.getId(),
                playlist.getName(),
                playlist.getDescription(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt());
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record PlaylistDetails(UUID id, String name, String description, Instant createdAt, Instant updatedAt) {
    }
}
