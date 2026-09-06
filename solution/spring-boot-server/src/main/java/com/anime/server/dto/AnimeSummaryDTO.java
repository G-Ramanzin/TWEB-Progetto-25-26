package com.anime.server.dto;

/**
 * Lightweight anime projection used by the batch endpoint to enrich
 * lists of anime ids (e.g. MongoDB recommendations) with titles and
 * images in a single round trip.
 *
 * @param animeId  MyAnimeList anime id
 * @param title    anime title
 * @param imageUrl poster URL
 * @param score    community score
 * @param type     format (TV, Movie, ...)
 */
public record AnimeSummaryDTO(
        Long animeId,
        String title,
        String imageUrl,
        Double score,
        String type) {
}
