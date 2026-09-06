package com.anime.server.repository;

import com.anime.server.model.CharacterNickname;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Spring Data repository for {@link CharacterNickname}. */
@Repository
public interface CharacterNicknameRepository extends JpaRepository<CharacterNickname, Long> {

    List<CharacterNickname> findByCharacterId(Long characterId);
}
