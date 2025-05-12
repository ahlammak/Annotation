package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.entities.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Integer> {
    /**
     * Trouve toutes les tâches associées à un dataset
     * @param dataSet Le dataset
     * @return Liste des tâches
     */
    List<Tache> findByData(DataSet dataSet);

    /**
     * Trouve toutes les tâches associées à un dataset et un annotateur
     * @param dataSet Le dataset
     * @param annotateur L'annotateur
     * @return Liste des tâches
     */
    List<Tache> findByDataAndAnnotateur(DataSet dataSet, Annotateur annotateur);

    /**
     * Trouve toutes les tâches associées à un annotateur
     * @param annotateur L'annotateur
     * @return Liste des tâches
     */
    List<Tache> findByAnnotateur(Annotateur annotateur);
}
