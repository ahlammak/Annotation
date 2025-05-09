package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.DataSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataSetRepository extends JpaRepository<DataSet, Integer> {

}
