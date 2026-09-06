package com.anime.server.dto;

/**
 * Projection for the "production works of a person" endpoint:
 * each staff position joined with the anime title.
 *
 * @param animeId  anime the position refers to
 * @param title    anime title (may be null for orphan rows)
 * @param position production position, e.g. "Director"
 */
public record ProductionWorkDTO(
        Long animeId,
        String title,
        String position) {
}
