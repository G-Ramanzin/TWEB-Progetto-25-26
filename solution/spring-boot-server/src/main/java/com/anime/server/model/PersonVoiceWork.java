package com.anime.server.model;

import jakarta.persistence.*;

/**
 * Join table mapping one row of {@code person_voice_works.csv} (real
 * headers: person_mal_id, role, anime_mal_id, character_mal_id,
 * language): a voice acting role linking a person, an anime and a
 * character in a given language.
 */
@Entity
@Table(name = "person_voice_works", indexes = {
        @Index(name = "idx_pvw_person", columnList = "person_id"),
        @Index(name = "idx_pvw_anime", columnList = "anime_id"),
        @Index(name = "idx_pvw_character", columnList = "character_id")
})
public class PersonVoiceWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "anime_id")
    private Long animeId;

    @Column(name = "character_id")
    private Long characterId;

    /** Voice role type (Main, Supporting). */
    private String role;

    /** Dub language, e.g. "Japanese". */
    private String language;

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public Long getAnimeId() { return animeId; }
    public void setAnimeId(Long animeId) { this.animeId = animeId; }
    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
