package com.anime.server.model;

import jakarta.persistence.*;

/**
 * JPA entity mapping one row of {@code details.csv} (real dataset
 * headers: mal_id, title, title_japanese, image_url, type, status,
 * score, scored_by, start_date, end_date, synopsis, rank, popularity,
 * members, favorites, genres, studios, themes, demographics, source,
 * rating, episodes, season, year, producers, licensors, streaming).
 * <p>
 * Long free-text fields are mapped as {@code TEXT} columns because
 * they routinely exceed the default {@code VARCHAR(255)} length.
 */
@Entity
@Table(name = "anime_details", indexes = {
        @Index(name = "idx_anime_title", columnList = "title"),
        @Index(name = "idx_anime_score", columnList = "score"),
        @Index(name = "idx_anime_type", columnList = "type")
})
public class AnimeDetail {

    /** MyAnimeList identifier of the anime (CSV column {@code mal_id}). */
    @Id
    @Column(name = "anime_id")
    private Long animeId;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(name = "title_japanese", columnDefinition = "TEXT")
    private String titleJapanese;

    /** TV, Movie, OVA, ONA, Special, Music... */
    private String type;

    /** Original source material (Manga, Light novel, Original...). */
    private String source;

    private Integer episodes;

    private String status;

    /** First air date, ISO timestamp string from the CSV. */
    @Column(name = "start_date")
    private String startDate;

    /** Last air date, ISO timestamp string from the CSV (may be empty). */
    @Column(name = "end_date")
    private String endDate;

    /** Airing season (winter/spring/summer/fall). */
    private String season;

    /** Airing year. */
    @Column(name = "air_year")
    private Integer year;

    /** Age rating, e.g. "R - 17+". */
    private String rating;

    /** Community score 1-10. */
    private Double score;

    @Column(name = "scored_by")
    private Integer scoredBy;

    /**
     * Ranking position. Named {@code anime_rank} because {@code rank}
     * clashes with the SQL window-function keyword.
     */
    @Column(name = "anime_rank")
    private Integer animeRank;

    private Integer popularity;

    private Integer members;

    private Integer favorites;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    /** Comma separated genre list as provided by the CSV. */
    @Column(columnDefinition = "TEXT")
    private String genres;

    /** Comma separated theme list (e.g. Mecha, School). */
    @Column(columnDefinition = "TEXT")
    private String themes;

    /** Comma separated demographic list (e.g. Shounen). */
    @Column(columnDefinition = "TEXT")
    private String demographics;

    @Column(columnDefinition = "TEXT")
    private String studios;

    @Column(columnDefinition = "TEXT")
    private String producers;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // --- getters / setters ---

    public Long getAnimeId() { return animeId; }
    public void setAnimeId(Long animeId) { this.animeId = animeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTitleJapanese() { return titleJapanese; }
    public void setTitleJapanese(String titleJapanese) { this.titleJapanese = titleJapanese; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getEpisodes() { return episodes; }
    public void setEpisodes(Integer episodes) { this.episodes = episodes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Integer getScoredBy() { return scoredBy; }
    public void setScoredBy(Integer scoredBy) { this.scoredBy = scoredBy; }
    public Integer getAnimeRank() { return animeRank; }
    public void setAnimeRank(Integer animeRank) { this.animeRank = animeRank; }
    public Integer getPopularity() { return popularity; }
    public void setPopularity(Integer popularity) { this.popularity = popularity; }
    public Integer getMembers() { return members; }
    public void setMembers(Integer members) { this.members = members; }
    public Integer getFavorites() { return favorites; }
    public void setFavorites(Integer favorites) { this.favorites = favorites; }
    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }
    public String getThemes() { return themes; }
    public void setThemes(String themes) { this.themes = themes; }
    public String getDemographics() { return demographics; }
    public void setDemographics(String demographics) { this.demographics = demographics; }
    public String getStudios() { return studios; }
    public void setStudios(String studios) { this.studios = studios; }
    public String getProducers() { return producers; }
    public void setProducers(String producers) { this.producers = producers; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
