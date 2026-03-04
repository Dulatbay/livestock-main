package kz.livestock.main.news.domain;

import kz.livestock.main.news.rest.dtos.NewsResponse;
import org.springframework.stereotype.Component;

@Component
class NewsMapper {

    NewsResponse toResponse(NewsEntity entity) {
        return new NewsResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
