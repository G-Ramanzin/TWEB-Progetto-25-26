package com.anime.server.controller;

import com.anime.server.dto.AnimeSummaryDTO;
import com.anime.server.dto.CharacterInAnimeDTO;
import com.anime.server.model.AnimeDetail;
import com.anime.server.service.AnimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for anime data. Base path: {@code /api/anime}.
 * All list endpoints are paginated and capped to protect the server.
 */
@RestController
@RequestMapping("/api/anime")
@Tag(name = "Anime", description = "Static anime data stored in PostgreSQL")
public class AnimeController {

    /** Hard cap applied to every client supplied page size. */
    private static final int MAX_PAGE_SIZE = 100;

    private final AnimeService animeService;

    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search anime",
            description = "Free text search on titles with optional genre/type/status filters, paginated")
    public Page<AnimeDetail> search(
            @Parameter(description = "Text matched against titles") @RequestParam(required = false) String q,
            @Parameter(description = "Genre filter, e.g. Action") @RequestParam(required = false) String genre,
            @Parameter(description = "Format filter, e.g. TV") @RequestParam(required = false) String type,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @Parameter(description = "0-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {
        return animeService.search(q, genre, type, status, page, Math.min(size, MAX_PAGE_SIZE));
    }

    @GetMapping("/top")
    @Operation(summary = "Top rated anime")
    public List<AnimeDetail> top(
            @Parameter(description = "Number of results (max 50)") @RequestParam(defaultValue = "10") int limit) {
        return animeService.topRated(Math.min(limit, 50));
    }

    @GetMapping("/batch")
    @Operation(summary = "Batch summaries",
            description = "Lightweight id/title/image projections for a comma separated list of ids. " +
                    "Used to enrich MongoDB responses (e.g. recommendations) with titles.")
    public List<AnimeSummaryDTO> batch(
            @Parameter(description = "Comma separated anime ids") @RequestParam List<Long> ids) {
        return animeService.summaries(ids.stream().limit(200).toList());
    }

    @GetMapping("/genres")
    @Operation(summary = "All distinct genres")
    public List<String> genres() {
        return animeService.allGenres();
    }

    @GetMapping("/types")
    @Operation(summary = "All distinct formats")
    public List<String> types() {
        return animeService.allTypes();
    }

    @GetMapping("/genre-stats")
    @Operation(summary = "Anime count per genre")
    public List<Map<String, Object>> genreStats() {
        return animeService.genreStats();
    }

    @GetMapping("/type-stats")
    @Operation(summary = "Anime count per format")
    public List<Map<String, Object>> typeStats() {
        return animeService.typeStats();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Anime detail by id")
    public ResponseEntity<AnimeDetail> byId(@PathVariable Long id) {
        return animeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/characters")
    @Operation(summary = "Characters of an anime",
            description = "Join of the character/anime relation with character names and images")
    public List<CharacterInAnimeDTO> characters(@PathVariable Long id) {
        return animeService.charactersOf(id);
    }
}
