package com.example.result.models;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.KeyStore;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Journal {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;

    private String text;

    private String photoLink;

    private boolean archived;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "journal_tags",
            joinColumns = @JoinColumn(name = "journal_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    @ManyToMany
    @JoinTable(
            name = "journal_collaborators",
            joinColumns = @JoinColumn(name = "journal_id"),
            inverseJoinColumns = @JoinColumn(name = "collaborator_id")
    )
    private Set<User> collaborators;
}
