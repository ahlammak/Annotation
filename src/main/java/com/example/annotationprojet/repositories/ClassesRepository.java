package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.Classes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassesRepository extends JpaRepository<Classes, Integer> {
    // Méthodes personnalisées si nécessaire
}
