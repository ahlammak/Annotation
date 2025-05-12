package com.example.annotationprojet.entities;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString @Builder
public class DataSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer ID;
    private String nom;
    private String description;
    private String url;
    private Boolean isProcessing = false;

    @OneToMany(mappedBy = "dataSet", cascade = CascadeType.REMOVE)
    private List<Classes> classes;

    @OneToMany(mappedBy = "data", fetch = FetchType.LAZY)
    private List<Tache> taches;

    @OneToMany(mappedBy = "dataSet", cascade = CascadeType.REMOVE)
    private List<coupleTexte> coupleTexte;
}
