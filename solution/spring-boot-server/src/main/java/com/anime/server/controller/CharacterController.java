package com.anime.server.controller;

import com.anime.server.dto.CharacterAppearanceDTO;
import com.anime.server.model.AnimeCharacter;
import com.anime.server.service.CharacterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST endpoints for characters. Base path: {@code /api/characters}. */
@RestController
@RequestMapping("/api/characters")
@Tag(name = "Characters", description = "Character data stored in PostgreSQL")
public class CharacterController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search characters by name",
            description = "With a blank query the most popular characters are returned")
    public Page<AnimeCharacter> search(
            @Parameter(description = "Text matched against the name") @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        return characterService.search(q, page, Math.min(size, MAX_PAGE_SIZE));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Character detail by id")
    public ResponseEntity<AnimeCharacter> byId(@PathVariable Long id) {
        return characterService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/nicknames")
    @Operation(summary = "Nicknames of a character")
    public List<String> nicknames(@PathVariable Long id) {
        return characterService.nicknamesOf(id);
    }

    @GetMapping("/{id}/appearances")
    @Operation(summary = "Anime appearances of a character",
            description = "Join with anime titles, posters and scores")
    public List<CharacterAppearanceDTO> appearances(@PathVariable Long id) {
        return characterService.appearancesOf(id);
    }
}
