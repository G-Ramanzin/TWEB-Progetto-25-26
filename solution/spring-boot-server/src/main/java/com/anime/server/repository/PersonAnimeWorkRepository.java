package com.anime.server.repository;

import com.anime.server.dto.ProductionWorkDTO;
import com.anime.server.model.PersonAnimeWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Spring Data repository for {@link PersonAnimeWork}. */
@Repository
public interface PersonAnimeWorkRepository extends JpaRepository<PersonAnimeWork, Long> {

    /**
     * Production works of a person joined with the anime title.
     * LEFT JOIN keeps rows whose anime is missing from details.csv.
     */
    @Query("""
            SELECT new com.anime.server.dto.ProductionWorkDTO(
                w.animeId, a.title, w.position)
            FROM PersonAnimeWork w
            LEFT JOIN AnimeDetail a ON a.animeId = w.animeId
            WHERE w.personId = :personId
            ORDER BY a.score DESC NULLS LAST
            """)
    List<ProductionWorkDTO> findProductionWorks(@Param("personId") Long personId);
}
