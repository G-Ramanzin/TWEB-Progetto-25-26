package com.anime.server.dto;

/**
 * Projection returned by the "characters of an anime" endpoint:
 * the character identity joined with the role it plays in that anime.
 * <p>
 * Using a DTO (instead of exposing raw join-table rows) lets the
 * frontend show character names and images without extra requests.
 *
 * @param characterId MyAnimeList character id
 * @param name        character name
 * @param imageUrl    character portrait URL (may be null)
 * @param role        role in the anime (Main / Supporting)
 * @param favorites   popularity of the character
 */
public record CharacterInAnimeDTO(
        Long characterId,
        String name,
        String imageUrl,
        String role,
        Integer favorites) {
}
