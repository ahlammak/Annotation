package com.example.annotationprojet.entities;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@NoArgsConstructor  @AllArgsConstructor  @Getter @Setter @ToString @Builder
public class Classes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;
    private String nomClasse;
    @ManyToOne
    private DataSet dataSet;

}
