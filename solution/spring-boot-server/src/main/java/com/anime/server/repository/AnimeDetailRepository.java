package com.anime.server.repository;

import com.anime.server.dto.AnimeSummaryDTO;
import com.anime.server.model.AnimeDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/** Spring Data repository for {@link AnimeDetail}. */
@Repository
public interface AnimeDetailRepository extends JpaRepository<AnimeDetail, Long> {

    /**
     * Full search with optional filters. Every parameter may be null,
     * in which case the corresponding filter is ignored. The text is
     * matched against both the main and the Japanese title.
     */
    @Query("""
            SELECT a FROM AnimeDetail a
            WHERE (:q IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :q, '%'))
                             OR LOWER(a.titleJapanese) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:genre IS NULL OR LOWER(a.genres) LIKE LOWER(CONCAT('%', :genre, '%')))
              AND (:type IS NULL OR a.type = :type)
              AND (:status IS NULL OR a.status = :status)
            """)
    Page<AnimeDetail> search(@Param("q") String q,
                             @Param("genre") String genre,
                             @Param("type") String type,
                             @Param("status") String status,
                             Pageable pageable);

    /** Top rated anime having a score, ordered by score descending. */
    @Query("SELECT a FROM AnimeDetail a WHERE a.score IS NOT NULL ORDER BY a.score DESC")
    List<AnimeDetail> findTopRated(Pageable pageable);

    /** All non-null genre strings (comma separated, parsed by the service). */
    @Query("SELECT a.genres FROM AnimeDetail a WHERE a.genres IS NOT NULL")
    List<String> findAllGenreStrings();

    /** Distinct anime formats present in the dataset. */
    @Query("SELECT DISTINCT a.type FROM AnimeDetail a WHERE a.type IS NOT NULL ORDER BY a.type")
    List<String> findAllTypes();

    /** Count of anime per format, ordered by count. */
    @Query("SELECT a.type, COUNT(a) FROM AnimeDetail a WHERE a.type IS NOT NULL GROUP BY a.type ORDER BY COUNT(a) DESC")
    List<Object[]> countByType();

    /** Lightweight summaries for a batch of ids (enrichment endpoint). */
    @Query("""
            SELECT new com.anime.server.dto.AnimeSummaryDTO(
                a.animeId, a.title, a.imageUrl, a.score, a.type)
            FROM AnimeDetail a WHERE a.animeId IN :ids
            """)
    List<AnimeSummaryDTO> findSummariesByIds(@Param("ids") Collection<Long> ids);
}
