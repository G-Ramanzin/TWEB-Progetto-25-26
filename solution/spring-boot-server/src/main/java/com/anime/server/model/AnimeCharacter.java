package com.anime.server.model;

import jakarta.persistence.*;

/**
 * JPA entity mapping one row of {@code characters.csv}.
 * <p>
 * The class is named {@code AnimeCharacter} (table {@code characters})
 * for two reasons: it avoids shadowing {@link java.lang.Character} and
 * it avoids the PostgreSQL reserved word {@code character} as table name.
 */
@Entity
@Table(name = "characters", indexes = {
        @Index(name = "idx_character_name", columnList = "name"),
        @Index(name = "idx_character_favorites", columnList = "favorites")
})
public class AnimeCharacter {

    /** MyAnimeList character identifier (primary key, from the CSV). */
    @Id
    @Column(name = "character_id")
    private Long characterId;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(name = "name_kanji", columnDefinition = "TEXT")
    private String nameKanji;

    /** Number of users that added the character to their favourites. */
    private Integer favorites;

    @Column(columnDefinition = "TEXT")
    private String about;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // --- getters / setters ---

    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNameKanji() { return nameKanji; }
    public void setNameKanji(String nameKanji) { this.nameKanji = nameKanji; }
    public Integer getFavorites() { return favorites; }
    public void setFavorites(Integer favorites) { this.favorites = favorites; }
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
