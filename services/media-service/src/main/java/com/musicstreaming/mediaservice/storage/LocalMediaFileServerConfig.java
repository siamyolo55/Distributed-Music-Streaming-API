package com.musicstreaming.mediaservice.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "media.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalMediaFileServerConfig implements WebMvcConfigurer {

    private final MediaStorageProperties properties;

    public LocalMediaFileServerConfig(MediaStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + ensureTrailingSlash(properties.getLocal().getRoot());
        registry.addResourceHandler("/local-media/**")
                .addResourceLocations(location);
    }

    private String ensureTrailingSlash(String value) {
        if (value.endsWith("/") || value.endsWith("\\")) {
            return value;
        }
        return value + "/";
    }
}
