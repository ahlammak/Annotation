package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.DataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncDataSetService {

    @Autowired
    private DataSetService dataSetService;

    @Async
    public void processDataSetAsync(DataSet dataset) {
        try {
            dataset.setIsProcessing(true);
            dataSetService.saveDataSet(dataset);

            System.out.println("Début du traitement asynchrone pour le dataset: " + dataset.getNom());
            dataSetService.processDataSetFile(dataset);
            System.out.println("Traitement asynchrone terminé pour le dataset: " + dataset.getNom());

            dataset.setIsProcessing(false);
            dataSetService.saveDataSet(dataset);
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement asynchrone du dataset: " + e.getMessage());
            e.printStackTrace();


            dataset.setIsProcessing(false);
            dataSetService.saveDataSet(dataset);
        }
    }
}
