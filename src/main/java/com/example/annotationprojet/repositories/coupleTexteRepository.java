package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.coupleTexte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface coupleTexteRepository extends JpaRepository<coupleTexte, Integer> {
}
