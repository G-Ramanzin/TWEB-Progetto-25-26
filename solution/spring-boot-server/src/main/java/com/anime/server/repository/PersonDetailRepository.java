package com.anime.server.repository;

import com.anime.server.model.PersonDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data repository for {@link PersonDetail}. */
@Repository
public interface PersonDetailRepository extends JpaRepository<PersonDetail, Long> {

    /** Case-insensitive name search, most popular people first. */
    @Query("""
            SELECT p FROM PersonDetail p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY p.favorites DESC NULLS LAST
            """)
    Page<PersonDetail> searchByName(@Param("q") String q, Pageable pageable);

    /** People ordered by popularity (default browsing view). */
    @Query("SELECT p FROM PersonDetail p ORDER BY p.favorites DESC NULLS LAST")
    Page<PersonDetail> findAllByPopularity(Pageable pageable);
}
