package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.DataSet;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DataSetService {
    DataSet importDataSet(MultipartFile file, String nom, String description, String classes) throws IOException;

    // Nouvelle méthode pour import direct et rapide (comme avant)
    DataSet importDataSetWithProcessing(MultipartFile file, String nom, String description, String classes) throws IOException;

    void processDataSetFile(DataSet dataset) throws IOException;

    List<DataSet> getAllDataSets();
    DataSet getDataSetById(Integer id);
    DataSet saveDataSet(DataSet dataSet);
    void deleteDataSet(Integer id);
}
