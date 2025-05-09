package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.DataSet;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DataSetService {

    /**
     * Crée un nouveau dataset avec les classes spécifiées
     * @param nom Le nom du dataset
     * @param description La description du dataset
     * @param classes Les classes séparées par des virgules
     * @return Le dataset créé
     */
    DataSet createDataSet(String nom, String description, String classes);

    /**
     * Importe un fichier CSV et crée un dataset avec les classes extraites
     * @param file Le fichier CSV à importer
     * @return Le dataset créé
     * @throws IOException En cas d'erreur lors de la lecture du fichier
     */
    DataSet importCsvFile(MultipartFile file) throws IOException;

    /**
     * Récupère tous les datasets
     * @return Liste de tous les datasets
     */
    List<DataSet> getAllDataSets();

    /**
     * Récupère un dataset par son ID
     * @param id L'ID du dataset
     * @return Le dataset trouvé ou null
     */
    DataSet getDataSetById(int id);

    /**
     * Sauvegarde un dataset
     * @param dataSet Le dataset à sauvegarder
     * @return Le dataset sauvegardé
     */
    DataSet saveDataSet(DataSet dataSet);

    /**
     * Supprime un dataset
     * @param id L'ID du dataset à supprimer
     */
    void deleteDataSet(int id);
}
