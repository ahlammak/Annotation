package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.Annotateur;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface AnnotateurRepository extends JpaRepository<Annotateur, Integer> {

    Page<Annotateur> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseAndActiveTrue(
            String nom, String prenom, Pageable pageable);

    Page<Annotateur> findByActiveTrue(Pageable pageable);

    List<Annotateur> findByActiveTrue();

    /**
     * Trouve un annotateur par son login
     * @param login Le login de l'annotateur
     * @return L'annotateur correspondant ou null
     */
    Annotateur findByLogin(String login);
}
