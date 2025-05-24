package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.entities.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Integer> {

    List<Tache> findByData(DataSet dataSet);


    List<Tache> findByDataAndAnnotateur(DataSet dataSet, Annotateur annotateur);

    List<Tache> findByAnnotateur(Annotateur annotateur);
}
