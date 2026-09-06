package com.anime.server.model;

import jakarta.persistence.*;

/**
 * JPA entity mapping one row of {@code person_details.csv} (real
 * headers: person_mal_id, url, website_url, image_url, name,
 * given_name, family_name, birthday, favorites, relevant_location).
 */
@Entity
@Table(name = "person_details", indexes = {
        @Index(name = "idx_person_name", columnList = "name"),
        @Index(name = "idx_person_favorites", columnList = "favorites")
})
public class PersonDetail {

    /** MyAnimeList person identifier (CSV column {@code person_mal_id}). */
    @Id
    @Column(name = "person_id")
    private Long personId;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(name = "given_name", columnDefinition = "TEXT")
    private String givenName;

    @Column(name = "family_name", columnDefinition = "TEXT")
    private String familyName;

    /** Birth date, ISO timestamp string from the CSV. */
    private String birthday;

    /** Number of users that added the person to their favourites. */
    private Integer favorites;

    /** Free-text location, e.g. "Tokyo, Japan". */
    @Column(columnDefinition = "TEXT")
    private String location;

    @Column(name = "website_url", columnDefinition = "TEXT")
    private String websiteUrl;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // --- getters / setters ---

    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }
    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public Integer getFavorites() { return favorites; }
    public void setFavorites(Integer favorites) { this.favorites = favorites; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
