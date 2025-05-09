package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.Annotateur;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface AnnotateurRepository extends JpaRepository<Annotateur, Integer> {
    // Méthode pour rechercher par mot-clé dans le nom ou le prénom avec active=true
    Page<Annotateur> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseAndActiveTrue(
            String nom, String prenom, Pageable pageable);

    // Méthode pour trouver tous les annotateurs actifs
    Page<Annotateur> findByActiveTrue(Pageable pageable);
}
