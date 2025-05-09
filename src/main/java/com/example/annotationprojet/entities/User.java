package com.example.annotationprojet.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString(exclude = "role") @Builder
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    @Id  @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ID;
    private String nom;
    private String prenom;
    private String login;
    private String password;
    @ManyToOne
    private Role role;
}
