package com.anime.server.controller;

import com.anime.server.dto.ProductionWorkDTO;
import com.anime.server.dto.VoiceWorkDTO;
import com.anime.server.model.PersonDetail;
import com.anime.server.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST endpoints for people. Base path: {@code /api/people}. */
@RestController
@RequestMapping("/api/people")
@Tag(name = "People", description = "Voice actors and staff stored in PostgreSQL")
public class PersonController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search people by name",
            description = "With a blank query the most popular people are returned")
    public Page<PersonDetail> search(
            @Parameter(description = "Text matched against the name") @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        return personService.search(q, page, Math.min(size, MAX_PAGE_SIZE));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Person detail by id")
    public ResponseEntity<PersonDetail> byId(@PathVariable Long id) {
        return personService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/alternate-names")
    @Operation(summary = "Alternate names of a person")
    public List<String> alternateNames(@PathVariable Long id) {
        return personService.alternateNamesOf(id);
    }

    @GetMapping("/{id}/anime-works")
    @Operation(summary = "Production works of a person",
            description = "Join with anime titles")
    public List<ProductionWorkDTO> animeWorks(@PathVariable Long id) {
        return personService.productionWorksOf(id);
    }

    @GetMapping("/{id}/voice-works")
    @Operation(summary = "Voice acting roles of a person",
            description = "Join with anime titles and character names")
    public List<VoiceWorkDTO> voiceWorks(@PathVariable Long id) {
        return personService.voiceWorksOf(id);
    }
}
