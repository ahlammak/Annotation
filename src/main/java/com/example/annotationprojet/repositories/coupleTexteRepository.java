package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.entities.Tache;
import com.example.annotationprojet.entities.coupleTexte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface coupleTexteRepository extends JpaRepository<coupleTexte, Integer> {

    @Modifying
    @Transactional
    @Query("DELETE FROM coupleTexte c WHERE c.dataSet = ?1")
    void deleteByDataSet(DataSet dataSet);

    List<coupleTexte> findByDataSet(DataSet dataSet);


    List<coupleTexte> findByTache(Tache tache);
}
