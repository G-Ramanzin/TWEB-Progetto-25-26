package com.anime.server.model;

import jakarta.persistence.*;

/** Join table mapping one row of {@code character_nicknames.csv}. */
@Entity
@Table(name = "character_nicknames", indexes = {
        @Index(name = "idx_nickname_character", columnList = "character_id")
})
public class CharacterNickname {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "character_id")
    private Long characterId;

    @Column(columnDefinition = "TEXT")
    private String nickname;

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
