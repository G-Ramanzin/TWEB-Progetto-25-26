package com.anime.server.model;

import jakarta.persistence.*;

/** Join table mapping one row of {@code person_alternate_names.csv}. */
@Entity
@Table(name = "person_alternate_names", indexes = {
        @Index(name = "idx_pan_person", columnList = "person_id")
})
public class PersonAlternateName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "alternate_name", columnDefinition = "TEXT")
    private String alternateName;

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getAlternateName() { return alternateName; }
    public void setAlternateName(String alternateName) { this.alternateName = alternateName; }
}
