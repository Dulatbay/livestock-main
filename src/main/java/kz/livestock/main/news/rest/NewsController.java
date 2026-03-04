package kz.livestock.main.news.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.livestock.main.news.domain.NewsService;
import kz.livestock.main.news.rest.dtos.CreateNewsRequest;
import kz.livestock.main.news.rest.dtos.NewsResponse;
import kz.livestock.main.news.rest.dtos.UpdateNewsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news")
@Tag(name = "News", description = "News management API")
class NewsController {
    private final NewsService newsService;

    NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    @Operation(summary = "List all news (paginated)")
    ResponseEntity<Page<NewsResponse>> listNews(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<NewsResponse> page = (query != null && !query.isBlank())
                ? newsService.searchNews(query, pageable)
                : newsService.listNews(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get news by ID")
    ResponseEntity<NewsResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @Operation(summary = "Create a news article")
    ResponseEntity<NewsResponse> create(@Valid @ModelAttribute CreateNewsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newsService.create(request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @Operation(summary = "Update a news article")
    ResponseEntity<NewsResponse> update(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateNewsRequest request) {
        return ResponseEntity.ok(newsService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_OPERATOR')")
    @Operation(summary = "Soft-delete a news article")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        newsService.delete(id);
    }
}
