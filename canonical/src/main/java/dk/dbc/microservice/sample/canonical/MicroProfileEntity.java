/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.microservice.sample.canonical;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "micro_profile")
@NamedQuery(name = MicroProfileEntity.FIND_ALL,
        query = "SELECT profile FROM MicroProfileEntity profile")
public class MicroProfileEntity {
    public static final String FIND_ALL = "MicroProfileEntity.findAll";

    @Id
    @SequenceGenerator(
            name = "micro_profile_id_seq",
            sequenceName = "micro_profile_id_seq",
            allocationSize = 1)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "micro_profile_id_seq")
    @Column(updatable = false)
    private Integer id;

    private String name;

    @Column(name = "source_repo")
    private String sourceRepo;

    private String description;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceRepo() {
        return sourceRepo;
    }

    public void setSourceRepo(String sourceRepo) {
        this.sourceRepo = sourceRepo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
