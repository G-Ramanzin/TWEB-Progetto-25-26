package com.anime.server.dto;

/**
 * Projection for the "voice works of a person" endpoint: each voice
 * acting role joined with both the anime title and character name.
 *
 * @param animeId       anime the role belongs to
 * @param animeTitle    title of the anime (may be null for orphan rows)
 * @param characterId   voiced character
 * @param characterName name of the voiced character (may be null)
 * @param role          role type (Main / Supporting)
 * @param language      dub language, e.g. "Japanese"
 */
public record VoiceWorkDTO(
        Long animeId,
        String animeTitle,
        Long characterId,
        String characterName,
        String role,
        String language) {
}
