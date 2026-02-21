package com.musicstreaming.mediaservice.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(name = "media.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFilesystemStorage implements MediaObjectStorage {

    private final Path rootPath;
    private final String publicBaseUrl;

    public LocalFilesystemStorage(MediaStorageProperties properties) {
        this.rootPath = Path.of(properties.getLocal().getRoot()).toAbsolutePath().normalize();
        this.publicBaseUrl = properties.getLocal().getPublicBaseUrl();
    }

    @Override
    public StoredObject store(String artistId, MultipartFile file) throws IOException {
        String objectKey = buildObjectKey(artistId, file.getOriginalFilename());
        Path destination = rootPath.resolve(objectKey).normalize();
        Files.createDirectories(destination.getParent());
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        String normalizedKey = objectKey.replace("\\", "/");
        String storagePath = "/local-media/" + normalizedKey;
        String publicUrl = trimTrailingSlash(publicBaseUrl) + "/" + normalizedKey;
        return new StoredObject(normalizedKey, storagePath, publicUrl);
    }

    private String buildObjectKey(String artistId, String originalFilename) {
        String safeArtistId = artistId.replaceAll("[^a-zA-Z0-9_-]", "_");
        String extension = extensionFrom(originalFilename);
        return "raw/" + safeArtistId + "/" + UUID.randomUUID() + extension;
    }

    private String extensionFrom(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".bin";
        }
        String ext = filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        return ext.matches("^\\.[a-z0-9]{1,8}$") ? ext : ".bin";
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
