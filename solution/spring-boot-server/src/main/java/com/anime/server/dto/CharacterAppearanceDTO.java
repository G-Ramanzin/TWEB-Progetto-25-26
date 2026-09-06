package com.anime.server.dto;

/**
 * Projection for the "anime appearances of a character" endpoint:
 * the anime identity joined with the character's role in it.
 *
 * @param animeId  MyAnimeList anime id
 * @param title    anime title
 * @param imageUrl anime poster URL (may be null)
 * @param score    community score of the anime
 * @param role     role of the character (Main / Supporting)
 */
public record CharacterAppearanceDTO(
        Long animeId,
        String title,
        String imageUrl,
        Double score,
        String role) {
}
