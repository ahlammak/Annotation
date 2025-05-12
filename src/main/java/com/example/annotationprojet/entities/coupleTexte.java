package com.example.annotationprojet.entities;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.*;

@Entity @NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString @Builder
public class coupleTexte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer ID;

    @Column(length = 1000)
    private String texte1;

    @Column(length = 1000)
    private String texte2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tache_id")
    private Tache tache;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotation_id")
    private Annotation annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    private DataSet dataSet;
}
