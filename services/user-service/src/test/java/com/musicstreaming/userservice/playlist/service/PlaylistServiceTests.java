package com.musicstreaming.userservice.playlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.musicstreaming.userservice.playlist.domain.Playlist;
import com.musicstreaming.userservice.playlist.domain.PlaylistRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTests {

    @Mock
    private PlaylistRepository playlistRepository;

    private PlaylistService playlistService;

    @BeforeEach
    void setUp() {
        playlistService = new PlaylistService(playlistRepository);
    }

    @Test
    void createTrimsNameAndNormalizesDescription() {
        UUID userId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        Instant now = Instant.now();
        Playlist saved = new Playlist(userId, "Road Trip", null, now, now);
        setId(saved, playlistId);
        when(playlistRepository.save(org.mockito.ArgumentMatchers.any(Playlist.class))).thenReturn(saved);

        PlaylistService.PlaylistDetails result = playlistService.create(userId, "  Road Trip  ", "   ");

        assertThat(result.id()).isEqualTo(playlistId);
        assertThat(result.name()).isEqualTo("Road Trip");
        assertThat(result.description()).isNull();
    }

    @Test
    void getReturnsNotFoundWhenPlaylistNotOwnedByUser() {
        UUID userId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.get(userId, playlistId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Playlist not found");
    }

    @Test
    void listMapsOwnedPlaylists() {
        UUID userId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        Instant now = Instant.now();
        Playlist playlist = new Playlist(userId, "Focus", "coding", now, now);
        setId(playlist, playlistId);
        when(playlistRepository.findAllByUserIdOrderByUpdatedAtDesc(userId)).thenReturn(List.of(playlist));

        List<PlaylistService.PlaylistDetails> result = playlistService.list(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(playlistId);
        assertThat(result.getFirst().name()).isEqualTo("Focus");
        assertThat(result.getFirst().description()).isEqualTo("coding");
    }

    @Test
    void updateModifiesNameAndDescription() {
        UUID userId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        Instant now = Instant.now();
        Playlist playlist = new Playlist(userId, "Old", "desc", now, now);
        setId(playlist, playlistId);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.of(playlist));

        PlaylistService.PlaylistDetails result = playlistService.update(userId, playlistId, " New ", "  updated ");

        assertThat(result.name()).isEqualTo("New");
        assertThat(result.description()).isEqualTo("updated");
    }

    @Test
    void deleteRemovesOwnedPlaylist() {
        UUID userId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        Instant now = Instant.now();
        Playlist playlist = new Playlist(userId, "Old", "desc", now, now);
        setId(playlist, playlistId);
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.of(playlist));

        playlistService.delete(userId, playlistId);

        verify(playlistRepository).delete(playlist);
    }

    @Test
    void deleteDoesNotCallRepositoryWhenPlaylistMissing() {
        UUID userId = UUID.randomUUID();
        UUID playlistId = UUID.randomUUID();
        when(playlistRepository.findByIdAndUserId(playlistId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.delete(userId, playlistId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Playlist not found");
        verify(playlistRepository, never()).delete(org.mockito.ArgumentMatchers.any(Playlist.class));
    }

    private static void setId(Playlist playlist, UUID id) {
        try {
            var field = Playlist.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(playlist, id);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
