package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.Classes;
import com.example.annotationprojet.entities.DataSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassesRepository extends JpaRepository<Classes, Integer> {

    List<Classes> findByDataSet(DataSet dataSet);
}
