package com.anime.server.repository;

import com.anime.server.model.PersonAlternateName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Spring Data repository for {@link PersonAlternateName}. */
@Repository
public interface PersonAlternateNameRepository extends JpaRepository<PersonAlternateName, Long> {

    List<PersonAlternateName> findByPersonId(Long personId);
}
