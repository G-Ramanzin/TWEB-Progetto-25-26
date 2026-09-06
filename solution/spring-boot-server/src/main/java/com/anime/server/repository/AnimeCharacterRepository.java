package com.anime.server.repository;

import com.anime.server.model.AnimeCharacter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data repository for {@link AnimeCharacter}. */
@Repository
public interface AnimeCharacterRepository extends JpaRepository<AnimeCharacter, Long> {

    /** Case-insensitive name search, most popular characters first. */
    @Query("""
            SELECT c FROM AnimeCharacter c
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY c.favorites DESC NULLS LAST
            """)
    Page<AnimeCharacter> searchByName(@Param("q") String q, Pageable pageable);

    /** Characters ordered by popularity (default browsing view). */
    @Query("SELECT c FROM AnimeCharacter c ORDER BY c.favorites DESC NULLS LAST")
    Page<AnimeCharacter> findAllByPopularity(Pageable pageable);
}
