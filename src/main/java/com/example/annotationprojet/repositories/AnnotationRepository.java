package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.Annotation;
import com.example.annotationprojet.entities.Annotateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Integer> {

    List<Annotation> findByAnnotateur(Annotateur annotateur);
}
