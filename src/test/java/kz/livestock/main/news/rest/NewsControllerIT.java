package kz.livestock.main.news.rest;

import kz.livestock.main.BaseIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NewsControllerIT extends BaseIT {

    // ==================== GET /news ====================

    @Test
    void shouldListNewsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldNotReturnDeletedNewsInList() throws Exception {
        mockMvc.perform(get("/news").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", not(hasItem(3))));
    }

    @Test
    void shouldSearchNewsByTitle() throws Exception {
        mockMvc.perform(get("/news").param("query", "Первая"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Первая новость"));
    }

    @Test
    void shouldSearchNewsByDescription() throws Exception {
        mockMvc.perform(get("/news").param("query", "КРС"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Вторая новость"));
    }

    @Test
    void shouldReturnEmptyPageForNonMatchingSearch() throws Exception {
        mockMvc.perform(get("/news").param("query", "несуществующий"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldRespectPagination() throws Exception {
        mockMvc.perform(get("/news").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ==================== GET /news/{id} ====================

    @Test
    void shouldGetNewsByIdWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/news/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Первая новость"))
                .andExpect(jsonPath("$.description").value("Описание первой новости о животноводстве"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/img1.jpg"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void shouldReturn404ForNonExistentNews() throws Exception {
        mockMvc.perform(get("/news/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404ForDeletedNews() throws Exception {
        mockMvc.perform(get("/news/3"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /news ====================

    @Test
    void shouldCreateNewsAsAdmin() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes());

        mockMvc.perform(multipart("/news")
                        .file(image)
                        .param("title", "Новая новость")
                        .param("description", "Описание новой новости")
                        .with(asAdmin()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Новая новость"))
                .andExpect(jsonPath("$.description").value("Описание новой новости"))
                .andExpect(jsonPath("$.imageUrl").value(startsWith("/files/")));
    }

    @Test
    void shouldCreateNewsAsOperator() throws Exception {
        mockMvc.perform(multipart("/news")
                        .param("title", "Новость от оператора")
                        .param("description", "Описание новости от оператора")
                        .with(asOperator()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrl").isEmpty());
    }

    @Test
    void shouldReturn401WhenCreatingNewsWithoutAuth() throws Exception {
        mockMvc.perform(multipart("/news")
                        .param("title", "Unauthorized")
                        .param("description", "Should fail"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenCreatingNewsAsRegularUser() throws Exception {
        mockMvc.perform(multipart("/news")
                        .param("title", "Forbidden")
                        .param("description", "Should fail")
                        .with(asUser()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingNewsWithBlankTitle() throws Exception {
        mockMvc.perform(multipart("/news")
                        .param("title", "")
                        .param("description", "Valid description")
                        .with(asAdmin()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingNewsWithoutDescription() throws Exception {
        mockMvc.perform(multipart("/news")
                        .param("title", "Valid title")
                        .with(asAdmin()))
                .andExpect(status().is4xxClientError());
    }

    // ==================== PUT /news/{id} ====================

    @Test
    void shouldUpdateNewsAsAdmin() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "updated.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image".getBytes());

        mockMvc.perform(multipart("/news/1")
                        .file(image)
                        .param("title", "Обновлённая новость")
                        .param("description", "Обновлённое описание")
                        .with(asAdmin())
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Обновлённая новость"))
                .andExpect(jsonPath("$.description").value("Обновлённое описание"))
                .andExpect(jsonPath("$.imageUrl").value(startsWith("/files/")));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentNews() throws Exception {
        mockMvc.perform(multipart("/news/999")
                        .param("title", "Updated")
                        .param("description", "Updated desc")
                        .with(asAdmin())
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenUpdatingNewsWithoutAuth() throws Exception {
        mockMvc.perform(multipart("/news/1")
                        .param("title", "Updated")
                        .param("description", "Updated desc")
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenUpdatingNewsAsRegularUser() throws Exception {
        mockMvc.perform(multipart("/news/1")
                        .param("title", "Updated")
                        .param("description", "Updated desc")
                        .with(asUser())
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE /news/{id} ====================

    @Test
    void shouldSoftDeleteNewsAsAdmin() throws Exception {
        mockMvc.perform(delete("/news/1").with(asAdmin()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/news/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentNews() throws Exception {
        mockMvc.perform(delete("/news/999").with(asAdmin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenDeletingNewsWithoutAuth() throws Exception {
        mockMvc.perform(delete("/news/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenDeletingNewsAsRegularUser() throws Exception {
        mockMvc.perform(delete("/news/1").with(asUser()))
                .andExpect(status().isForbidden());
    }
}
