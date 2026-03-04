package kz.livestock.main.filemanager.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file.storage")
public record FileStorageProperties(
        String uploadDir
) {}
