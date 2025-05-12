package com.example.annotationprojet.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString @Builder
public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer ID;
    private Date dateLimite;

    @OneToMany(mappedBy = "tache", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<coupleTexte> coupleTexte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_id")
    private DataSet data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotateur_id")
    private Annotateur annotateur;
}
