package com.anime.server.model;

import jakarta.persistence.*;

/**
 * Join table mapping one row of {@code character_anime_works.csv}:
 * the role a character plays in a given anime (Main / Supporting).
 * <p>
 * No foreign key constraints are declared on purpose: the CSV dumps
 * may contain orphan references and the import must not fail on them.
 */
@Entity
@Table(name = "character_anime_works", indexes = {
        @Index(name = "idx_caw_character", columnList = "character_id"),
        @Index(name = "idx_caw_anime", columnList = "anime_id")
})
public class CharacterAnimeWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "character_id")
    private Long characterId;

    @Column(name = "anime_id")
    private Long animeId;

    /** Role of the character in the anime (Main, Supporting). */
    private String role;

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }
    public Long getAnimeId() { return animeId; }
    public void setAnimeId(Long animeId) { this.animeId = animeId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
