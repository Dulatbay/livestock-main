package kz.livestock.main.news.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface NewsRepository extends JpaRepository<NewsEntity, Long> {

    Page<NewsEntity> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT n FROM NewsEntity n WHERE n.deleted = false AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(n.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY n.createdAt DESC")
    Page<NewsEntity> search(@Param("query") String query, Pageable pageable);

    Optional<NewsEntity> findByIdAndDeletedFalse(Long id);
}
