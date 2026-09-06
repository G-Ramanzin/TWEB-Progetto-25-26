package com.anime.server.repository;

import com.anime.server.dto.VoiceWorkDTO;
import com.anime.server.model.PersonVoiceWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Spring Data repository for {@link PersonVoiceWork}. */
@Repository
public interface PersonVoiceWorkRepository extends JpaRepository<PersonVoiceWork, Long> {

    /**
     * Voice acting roles of a person joined with anime titles and
     * character names. LEFT JOINs keep orphan rows visible.
     */
    @Query("""
            SELECT new com.anime.server.dto.VoiceWorkDTO(
                w.animeId, a.title, w.characterId, c.name, w.role, w.language)
            FROM PersonVoiceWork w
            LEFT JOIN AnimeDetail a ON a.animeId = w.animeId
            LEFT JOIN AnimeCharacter c ON c.characterId = w.characterId
            WHERE w.personId = :personId
            ORDER BY a.score DESC NULLS LAST
            """)
    List<VoiceWorkDTO> findVoiceWorks(@Param("personId") Long personId);
}
