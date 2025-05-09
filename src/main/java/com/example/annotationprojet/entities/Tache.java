package com.example.annotationprojet.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString @Builder
public class Tache {
    @Id
    private int ID;
    private Date dateLimite;
    @OneToMany(mappedBy = "tache")
    private List <coupleTexte> coupleTexte;
    @ManyToOne
    private DataSet data;
    @ManyToOne
    private Annotateur annotateur;
}
