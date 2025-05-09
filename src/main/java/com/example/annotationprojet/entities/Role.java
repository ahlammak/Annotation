package com.example.annotationprojet.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString(exclude = "users") @Builder
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;
    private String nomRole;
    @OneToMany(mappedBy = "role",fetch = FetchType.EAGER)
    private List<User> users;
}
