package com.musicstreaming.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.musicstreaming.userservice.domain.UserAccount;
import com.musicstreaming.userservice.domain.UserAccountRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserDirectoryServiceTests {

    @Mock
    private UserAccountRepository userAccountRepository;

    private UserDirectoryService service;

    @BeforeEach
    void setUp() {
        service = new UserDirectoryService(userAccountRepository);
    }

    @Test
    void listDiscoverableUsersExcludesCurrentUserAndMapsFields() {
        UUID currentUserId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        UserAccount account = new UserAccount("artist@example.com", "hash", "Artist One", createdAt);

        setId(account, userId);
        when(userAccountRepository.findAllByIdNotOrderByCreatedAtDesc(currentUserId))
                .thenReturn(List.of(account));

        List<UserDirectoryService.DiscoverableUser> result = service.listDiscoverableUsers(currentUserId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().userId()).isEqualTo(userId);
        assertThat(result.getFirst().displayName()).isEqualTo("Artist One");
        assertThat(result.getFirst().email()).isEqualTo("artist@example.com");
        assertThat(result.getFirst().createdAt()).isEqualTo(createdAt);
    }

    private static void setId(UserAccount account, UUID userId) {
        try {
            java.lang.reflect.Field field = UserAccount.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(account, userId);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to set test user id", ex);
        }
    }
}
