package com.anime.server.service;

import com.anime.server.dto.CharacterAppearanceDTO;
import com.anime.server.model.AnimeCharacter;
import com.anime.server.model.CharacterNickname;
import com.anime.server.repository.AnimeCharacterRepository;
import com.anime.server.repository.CharacterAnimeWorkRepository;
import com.anime.server.repository.CharacterNicknameRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Business logic for character browsing and detail pages. */
@Service
public class CharacterService {

    private final AnimeCharacterRepository characterRepo;
    private final CharacterAnimeWorkRepository workRepo;
    private final CharacterNicknameRepository nicknameRepo;

    public CharacterService(AnimeCharacterRepository characterRepo,
                            CharacterAnimeWorkRepository workRepo,
                            CharacterNicknameRepository nicknameRepo) {
        this.characterRepo = characterRepo;
        this.workRepo = workRepo;
        this.nicknameRepo = nicknameRepo;
    }

    /**
     * Search characters by name; with a blank query the most popular
     * characters are returned instead.
     */
    public Page<AnimeCharacter> search(String q, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) {
            return characterRepo.findAllByPopularity(pageable);
        }
        return characterRepo.searchByName(q.trim(), pageable);
    }

    /** @return the character with the given id, if present */
    public Optional<AnimeCharacter> findById(Long id) {
        return characterRepo.findById(id);
    }

    /** Nickname strings of a character. */
    public List<String> nicknamesOf(Long characterId) {
        return nicknameRepo.findByCharacterId(characterId).stream()
                .map(CharacterNickname::getNickname)
                .collect(Collectors.toList());
    }

    /** Anime the character appears in (joined with titles). */
    public List<CharacterAppearanceDTO> appearancesOf(Long characterId) {
        return workRepo.findAppearancesOfCharacter(characterId);
    }
}
