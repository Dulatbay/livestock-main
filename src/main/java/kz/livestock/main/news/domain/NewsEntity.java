package kz.livestock.main.news.domain;

import jakarta.persistence.*;
import kz.livestock.main.shared.domain.BaseEntity;
import lombok.Getter;

@Entity
@Table(name = "news")
@Getter
class NewsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "last_modified_keycloak_id")
    private String lastModifiedKeycloakId;

    protected NewsEntity() {}

    NewsEntity(String title, String description, String imageUrl, String keycloakId) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.deleted = false;
        this.lastModifiedKeycloakId = keycloakId;
    }

    void update(String title, String description, String imageUrl, String keycloakId) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.lastModifiedKeycloakId = keycloakId;
    }

    void softDelete(String keycloakId) {
        this.deleted = true;
        this.lastModifiedKeycloakId = keycloakId;
    }
}
