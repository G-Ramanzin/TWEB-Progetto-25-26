package com.anime.server.model;

import jakarta.persistence.*;

/**
 * Join table mapping one row of {@code person_anime_works.csv}:
 * the production role of a person in an anime (Director, Music, ...).
 */
@Entity
@Table(name = "person_anime_works", indexes = {
        @Index(name = "idx_paw_person", columnList = "person_id"),
        @Index(name = "idx_paw_anime", columnList = "anime_id")
})
public class PersonAnimeWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "anime_id")
    private Long animeId;

    /**
     * Production position, e.g. "Director", "Theme Song Performance".
     * Stored as {@code work_position}: {@code position} is a reserved
     * word in PostgreSQL and cannot be used unquoted.
     */
    @Column(name = "work_position", columnDefinition = "TEXT")
    private String position;

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public Long getAnimeId() { return animeId; }
    public void setAnimeId(Long animeId) { this.animeId = animeId; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}
