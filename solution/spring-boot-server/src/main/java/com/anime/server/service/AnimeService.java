package com.anime.server.service;

import com.anime.server.dto.AnimeSummaryDTO;
import com.anime.server.dto.CharacterInAnimeDTO;
import com.anime.server.model.AnimeDetail;
import com.anime.server.repository.AnimeDetailRepository;
import com.anime.server.repository.CharacterAnimeWorkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Business logic for anime data: search with filters, detail lookup,
 * genre/type statistics and batch summaries.
 */
@Service
public class AnimeService {

    private final AnimeDetailRepository animeRepo;
    private final CharacterAnimeWorkRepository characterWorkRepo;

    public AnimeService(AnimeDetailRepository animeRepo,
                        CharacterAnimeWorkRepository characterWorkRepo) {
        this.animeRepo = animeRepo;
        this.characterWorkRepo = characterWorkRepo;
    }

    /**
     * Search anime with optional filters; blank filters are ignored.
     * Ordering (score DESC, nulls last) is defined inside the JPQL
     * query: Spring Data ignores {@code NullHandling} on JPQL sorts.
     */
    public Page<AnimeDetail> search(String q, String genre, String type, String status,
                                    int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return animeRepo.search(blankToNull(q), blankToNull(genre),
                blankToNull(type), blankToNull(status), pageable);
    }

    /** @return the anime with the given id, if present */
    public Optional<AnimeDetail> findById(Long id) {
        return animeRepo.findById(id);
    }

    /** @return the {@code limit} best scored anime */
    public List<AnimeDetail> topRated(int limit) {
        return animeRepo.findTopRated(PageRequest.of(0, limit));
    }

    /** Characters of an anime (joined with names and images). */
    public List<CharacterInAnimeDTO> charactersOf(Long animeId) {
        return characterWorkRepo.findCharactersOfAnime(animeId);
    }

    /** Distinct genres parsed from the comma-separated CSV column. */
    public List<String> allGenres() {
        return animeRepo.findAllGenreStrings().stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(AnimeService::cleanListToken)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** Distinct anime formats. */
    public List<String> allTypes() {
        return animeRepo.findAllTypes();
    }

    /** Number of anime per genre, ordered by count descending. */
    public List<Map<String, Object>> genreStats() {
        Map<String, Integer> counts = new HashMap<>();
        animeRepo.findAllGenreStrings().forEach(s ->
                Arrays.stream(s.split(","))
                        .map(AnimeService::cleanListToken)
                        .filter(g -> !g.isEmpty())
                        .forEach(g -> counts.merge(g, 1, Integer::sum)));

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> Map.<String, Object>of("genre", e.getKey(), "count", e.getValue()))
                .collect(Collectors.toList());
    }

    /** Number of anime per format, ordered by count descending. */
    public List<Map<String, Object>> typeStats() {
        return animeRepo.countByType().stream()
                .map(row -> Map.<String, Object>of("type", row[0], "count", row[1]))
                .collect(Collectors.toList());
    }

    /** Lightweight summaries for a batch of ids (order not guaranteed). */
    public List<AnimeSummaryDTO> summaries(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return animeRepo.findSummariesByIds(ids);
    }

    /**
     * Cleans one token of a CSV list column: the dataset stores lists
     * as Python-style strings like {@code ['Action', 'Adventure']},
     * so brackets and quotes must be stripped.
     */
    private static String cleanListToken(String s) {
        return s == null ? "" : s.replaceAll("[\\[\\]'\"]", "").trim();
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
