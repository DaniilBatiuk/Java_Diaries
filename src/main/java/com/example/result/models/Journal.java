package com.example.result.models;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.security.KeyStore;
import java.sql.Timestamp;
import java.time.LocalDate;
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

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate archiveTimestamp;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @OneToMany
    @JoinColumn(name = "journal_id")
    private Set<Tag> tags;


    @ManyToOne
    @JoinColumn(name = "collaborator_id")
    private User collaborator;

}
