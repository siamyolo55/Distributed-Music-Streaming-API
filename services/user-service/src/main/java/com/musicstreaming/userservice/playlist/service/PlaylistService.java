package com.musicstreaming.userservice.playlist.service;

import com.musicstreaming.userservice.playlist.domain.Playlist;
import com.musicstreaming.userservice.playlist.domain.PlaylistRepository;
import com.musicstreaming.userservice.playlist.domain.PlaylistTrack;
import com.musicstreaming.userservice.playlist.domain.PlaylistTrackRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;

    public PlaylistService(PlaylistRepository playlistRepository, PlaylistTrackRepository playlistTrackRepository) {
        this.playlistRepository = playlistRepository;
        this.playlistTrackRepository = playlistTrackRepository;
    }

    @Transactional
    public PlaylistDetails create(UUID userId, String name, String description, List<PlaylistTrackInput> tracks) {
        Instant now = Instant.now();
        Playlist saved = playlistRepository.save(new Playlist(userId, name.trim(), normalizeDescription(description), now, now));
        List<PlaylistTrackDetails> trackItems = replaceTracks(saved.getId(), tracks);
        return toDetails(saved, trackItems);
    }

    @Transactional(readOnly = true)
    public List<PlaylistDetails> list(UUID userId) {
        List<Playlist> playlists = playlistRepository.findAllByUserIdOrderByUpdatedAtDesc(userId);
        if (playlists.isEmpty()) {
            return List.of();
        }

        List<UUID> playlistIds = playlists.stream().map(Playlist::getId).toList();
        Map<UUID, List<PlaylistTrackDetails>> trackMap = buildTrackMap(playlistTrackRepository.findAllByPlaylistIdInOrderByPlaylistIdAscPositionAsc(playlistIds));

        return playlists.stream()
                .map(playlist -> toDetails(playlist, trackMap.getOrDefault(playlist.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaylistDetails get(UUID userId, UUID playlistId) {
        Playlist playlist = findOwnedPlaylist(userId, playlistId);
        List<PlaylistTrackDetails> tracks = playlistTrackRepository.findAllByPlaylistIdOrderByPositionAsc(playlistId).stream()
                .map(this::toTrackDetails)
                .toList();
        return toDetails(playlist, tracks);
    }

    @Transactional
    public PlaylistDetails update(UUID userId, UUID playlistId, String name, String description, List<PlaylistTrackInput> tracks) {
        Playlist playlist = findOwnedPlaylist(userId, playlistId);
        playlist.update(name.trim(), normalizeDescription(description), Instant.now());
        List<PlaylistTrackDetails> existingTracks = playlistTrackRepository.findAllByPlaylistIdOrderByPositionAsc(playlistId).stream()
                .map(this::toTrackDetails)
                .toList();
        List<PlaylistTrackDetails> resolvedTracks = tracks == null ? existingTracks : replaceTracks(playlistId, tracks);
        return toDetails(playlist, resolvedTracks);
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

    private PlaylistDetails toDetails(Playlist playlist, List<PlaylistTrackDetails> tracks) {
        return new PlaylistDetails(
                playlist.getId(),
                playlist.getName(),
                playlist.getDescription(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt(),
                tracks);
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<PlaylistTrackDetails> replaceTracks(UUID playlistId, List<PlaylistTrackInput> tracks) {
        List<PlaylistTrackInput> normalized = normalizeTracks(tracks);
        playlistTrackRepository.deleteByPlaylistId(playlistId);
        if (normalized.isEmpty()) {
            return List.of();
        }

        Instant now = Instant.now();
        List<PlaylistTrack> entities = new ArrayList<>(normalized.size());
        for (int i = 0; i < normalized.size(); i++) {
            PlaylistTrackInput input = normalized.get(i);
            entities.add(new PlaylistTrack(
                    playlistId,
                    input.trackId(),
                    input.title(),
                    input.artistName(),
                    input.genre(),
                    i + 1,
                    now));
        }

        return playlistTrackRepository.saveAll(entities).stream()
                .map(this::toTrackDetails)
                .toList();
    }

    private List<PlaylistTrackInput> normalizeTracks(List<PlaylistTrackInput> tracks) {
        if (tracks == null || tracks.isEmpty()) {
            return List.of();
        }
        if (tracks.size() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playlist cannot exceed 500 tracks");
        }

        List<PlaylistTrackInput> normalized = new ArrayList<>(tracks.size());
        LinkedHashSet<String> seenTrackIds = new LinkedHashSet<>();
        for (PlaylistTrackInput track : tracks) {
            String trackId = normalizeRequired(track.trackId(), "Track id is required");
            if (!seenTrackIds.add(trackId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playlist contains duplicate track ids");
            }
            normalized.add(new PlaylistTrackInput(
                    trackId,
                    normalizeRequired(track.title(), "Track title is required"),
                    normalizeRequired(track.artistName(), "Artist name is required"),
                    normalizeRequired(track.genre(), "Genre is required")));
        }
        return normalized;
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private Map<UUID, List<PlaylistTrackDetails>> buildTrackMap(List<PlaylistTrack> tracks) {
        Map<UUID, List<PlaylistTrackDetails>> result = new LinkedHashMap<>();
        for (PlaylistTrack track : tracks) {
            result.computeIfAbsent(track.getPlaylistId(), ignored -> new ArrayList<>()).add(toTrackDetails(track));
        }
        return result;
    }

    private PlaylistTrackDetails toTrackDetails(PlaylistTrack track) {
        return new PlaylistTrackDetails(
                track.getTrackId(),
                track.getTitle(),
                track.getArtistName(),
                track.getGenre(),
                track.getPosition());
    }

    public record PlaylistDetails(
            UUID id,
            String name,
            String description,
            Instant createdAt,
            Instant updatedAt,
            List<PlaylistTrackDetails> tracks) {
    }

    public record PlaylistTrackDetails(String trackId, String title, String artistName, String genre, int position) {
    }

    public record PlaylistTrackInput(String trackId, String title, String artistName, String genre) {
    }
}
