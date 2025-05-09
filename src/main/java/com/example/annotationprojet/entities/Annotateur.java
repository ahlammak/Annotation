package com.example.annotationprojet.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.List;

@Entity @NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString
public class Annotateur extends User {
    @OneToMany(mappedBy = "annotateur")
    private List<Annotation> annotations;
    @OneToMany(mappedBy = "annotateur")
    private List<Tache> taches;
    private boolean active = true;

}
