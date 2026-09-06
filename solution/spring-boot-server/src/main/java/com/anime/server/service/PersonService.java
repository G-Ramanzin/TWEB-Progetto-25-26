package com.anime.server.service;

import com.anime.server.dto.ProductionWorkDTO;
import com.anime.server.dto.VoiceWorkDTO;
import com.anime.server.model.PersonAlternateName;
import com.anime.server.model.PersonDetail;
import com.anime.server.repository.PersonAlternateNameRepository;
import com.anime.server.repository.PersonAnimeWorkRepository;
import com.anime.server.repository.PersonDetailRepository;
import com.anime.server.repository.PersonVoiceWorkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Business logic for people (voice actors and staff). */
@Service
public class PersonService {

    private final PersonDetailRepository personRepo;
    private final PersonAnimeWorkRepository animeWorkRepo;
    private final PersonVoiceWorkRepository voiceWorkRepo;
    private final PersonAlternateNameRepository altNameRepo;

    public PersonService(PersonDetailRepository personRepo,
                         PersonAnimeWorkRepository animeWorkRepo,
                         PersonVoiceWorkRepository voiceWorkRepo,
                         PersonAlternateNameRepository altNameRepo) {
        this.personRepo = personRepo;
        this.animeWorkRepo = animeWorkRepo;
        this.voiceWorkRepo = voiceWorkRepo;
        this.altNameRepo = altNameRepo;
    }

    /**
     * Search people by name; with a blank query the most popular
     * people are returned instead.
     */
    public Page<PersonDetail> search(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) {
            return personRepo.findAllByPopularity(pageable);
        }
        return personRepo.searchByName(q.trim(), pageable);
    }

    /** @return the person with the given id, if present */
    public Optional<PersonDetail> findById(Long id) {
        return personRepo.findById(id);
    }

    /** Alternate name strings of a person. */
    public List<String> alternateNamesOf(Long personId) {
        return altNameRepo.findByPersonId(personId).stream()
                .map(PersonAlternateName::getAlternateName)
                .collect(Collectors.toList());
    }

    /** Production works joined with anime titles. */
    public List<ProductionWorkDTO> productionWorksOf(Long personId) {
        return animeWorkRepo.findProductionWorks(personId);
    }

    /** Voice roles joined with anime titles and character names. */
    public List<VoiceWorkDTO> voiceWorksOf(Long personId) {
        return voiceWorkRepo.findVoiceWorks(personId);
    }
}
