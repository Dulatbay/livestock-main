package kz.livestock.main.news.domain;

import kz.livestock.main.filemanager.domain.FileManagerService;
import kz.livestock.main.news.rest.dtos.CreateNewsRequest;
import kz.livestock.main.news.rest.dtos.NewsResponse;
import kz.livestock.main.news.rest.dtos.UpdateNewsRequest;
import kz.livestock.main.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class NewsService {
    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;
    private final FileManagerService fileManagerService;

    NewsService(NewsRepository newsRepository, NewsMapper newsMapper, FileManagerService fileManagerService) {
        this.newsRepository = newsRepository;
        this.newsMapper = newsMapper;
        this.fileManagerService = fileManagerService;
    }

    @Transactional(readOnly = true)
    public Page<NewsResponse> listNews(Pageable pageable) {
        return newsRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable)
                .map(newsMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NewsResponse> searchNews(String query, Pageable pageable) {
        return newsRepository.search(query, pageable)
                .map(newsMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public NewsResponse getById(Long id) {
        return newsRepository.findByIdAndDeletedFalse(id)
                .map(newsMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
    }

    @Transactional
    public NewsResponse create(CreateNewsRequest request) {
        String imageUrl = storeImage(request.image());
        var entity = new NewsEntity(
                request.title(),
                request.description(),
                imageUrl,
                getCurrentKeycloakId()
        );
        return newsMapper.toResponse(newsRepository.save(entity));
    }

    @Transactional
    public NewsResponse update(Long id, UpdateNewsRequest request) {
        var entity = newsRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));

        String imageUrl = entity.getImageUrl();
        if (request.image() != null && !request.image().isEmpty()) {
            deleteImage(imageUrl);
            imageUrl = storeImage(request.image());
        }

        entity.update(
                request.title(),
                request.description(),
                imageUrl,
                getCurrentKeycloakId()
        );
        return newsMapper.toResponse(newsRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        var entity = newsRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
        entity.softDelete(getCurrentKeycloakId());
        newsRepository.save(entity);
    }

    private static final String FILES_PATH_PREFIX = "/files/";

    private String storeImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        return FILES_PATH_PREFIX + fileManagerService.store(image);
    }

    private void deleteImage(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith(FILES_PATH_PREFIX)) {
            fileManagerService.delete(imageUrl.substring(FILES_PATH_PREFIX.length()));
        }
    }

    private String getCurrentKeycloakId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return null;
    }
}
