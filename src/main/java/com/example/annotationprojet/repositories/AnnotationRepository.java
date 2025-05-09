package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Integer> {
}
