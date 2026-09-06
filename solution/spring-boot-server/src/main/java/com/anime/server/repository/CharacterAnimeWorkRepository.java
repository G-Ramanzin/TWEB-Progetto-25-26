package com.anime.server.repository;

import com.anime.server.dto.CharacterAppearanceDTO;
import com.anime.server.dto.CharacterInAnimeDTO;
import com.anime.server.model.CharacterAnimeWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link CharacterAnimeWork}.
 * <p>
 * The two JPQL queries use entity joins on plain id columns
 * ({@code JOIN ... ON}) because the schema deliberately has no
 * declared foreign keys (orphan-tolerant CSV import).
 */
@Repository
public interface CharacterAnimeWorkRepository extends JpaRepository<CharacterAnimeWork, Long> {

    /** Characters appearing in an anime, joined with their identity. */
    @Query("""
            SELECT new com.anime.server.dto.CharacterInAnimeDTO(
                c.characterId, c.name, c.imageUrl, w.role, c.favorites)
            FROM CharacterAnimeWork w
            JOIN AnimeCharacter c ON c.characterId = w.characterId
            WHERE w.animeId = :animeId
            ORDER BY c.favorites DESC NULLS LAST
            """)
    List<CharacterInAnimeDTO> findCharactersOfAnime(@Param("animeId") Long animeId);

    /** Anime a character appears in, joined with the anime identity. */
    @Query("""
            SELECT new com.anime.server.dto.CharacterAppearanceDTO(
                a.animeId, a.title, a.imageUrl, a.score, w.role)
            FROM CharacterAnimeWork w
            JOIN AnimeDetail a ON a.animeId = w.animeId
            WHERE w.characterId = :characterId
            ORDER BY a.score DESC NULLS LAST
            """)
    List<CharacterAppearanceDTO> findAppearancesOfCharacter(@Param("characterId") Long characterId);
}
