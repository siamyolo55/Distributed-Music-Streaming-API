package com.musicstreaming.mediaservice.storage;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface MediaObjectStorage {

    StoredObject store(String artistId, MultipartFile file) throws IOException;

    record StoredObject(String objectKey, String storagePath, String publicUrl) {
    }
}
