package com.example.annotationprojet.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.*;

@Entity @NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString @Builder
public class coupleTexte {
    @Id
    private int ID;
    private String texte1;
    private String texte2;
    @ManyToOne
    private Tache tache;
    @OneToOne
    private Annotation annotation;
    @ManyToOne
    private DataSet dataSet;
}
