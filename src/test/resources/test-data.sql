INSERT INTO news (id, title, description, image_url, is_deleted, last_modified_keycloak_id, created_at, updated_at)
VALUES (1, 'Первая новость', 'Описание первой новости о животноводстве', 'https://example.com/img1.jpg', false, 'keycloak-user-1', NOW(), NOW());

INSERT INTO news (id, title, description, image_url, is_deleted, last_modified_keycloak_id, created_at, updated_at)
VALUES (2, 'Вторая новость', 'Описание второй новости о ценах КРС', null, false, 'keycloak-user-1', NOW(), NOW());

INSERT INTO news (id, title, description, image_url, is_deleted, last_modified_keycloak_id, created_at, updated_at)
VALUES (3, 'Удалённая новость', 'Эта новость была удалена', null, true, 'keycloak-user-1', NOW(), NOW());

ALTER SEQUENCE news_id_seq RESTART WITH 100;
