package com.example.annotationprojet.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.*;

@Entity @NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString @Builder
public class Annotation {
    @Id
    private int ID;
    private String typeChoisie;
    @OneToOne(mappedBy = "annotation")
    private coupleTexte coupleTexte;
    @ManyToOne
    private Annotateur annotateur;
}
