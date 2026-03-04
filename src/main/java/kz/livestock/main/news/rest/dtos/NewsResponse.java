package kz.livestock.main.news.rest.dtos;

import java.time.Instant;

public record NewsResponse(
        Long id,
        String title,
        String description,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt
) {}
