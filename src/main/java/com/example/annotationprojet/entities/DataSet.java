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
    private int ID;
    private String nom;
    private String description;
    private String url;
    @OneToMany(mappedBy = "dataSet")
    private List<Classes> classes;
    @OneToMany(mappedBy = "data")
    private  List<Tache>taches;
    @OneToMany(mappedBy = "dataSet")
    private List<coupleTexte> coupleTexte;
}
