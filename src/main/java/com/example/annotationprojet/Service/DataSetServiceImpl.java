package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.Classes;
import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.repositories.ClassesRepository;
import com.example.annotationprojet.repositories.DataSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DataSetServiceImpl implements DataSetService {

    @Autowired
    private DataSetRepository dataSetRepository;
    
    @Autowired
    private ClassesRepository classesRepository;
    
    @Override
    @Transactional
    public DataSet createDataSet(String nom, String description, String classes) {
        // Créer un nouveau dataset
        DataSet dataSet = new DataSet();
        dataSet.setNom(nom);
        dataSet.setDescription(description);
        
        // Sauvegarder le dataset
        DataSet savedDataSet = dataSetRepository.save(dataSet);
        
        // Traiter les classes si elles sont fournies
        if (classes != null && !classes.isEmpty()) {
            List<Classes> classesList = new ArrayList<>();
            String[] classesArray = classes.split(",");
            
            for (String className : classesArray) {
                Classes classEntity = new Classes();
                classEntity.setNomClasse(className.trim());
                classEntity.setDataSet(savedDataSet);
                classesList.add(classEntity);
            }
            
            // Sauvegarder les classes
            classesRepository.saveAll(classesList);
            savedDataSet.setClasses(classesList);
        }
        
        return savedDataSet;
    }
    
    @Override
    @Transactional
    public DataSet importCsvFile(MultipartFile file) throws IOException {
        // Vérifier que le fichier n'est pas vide
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }
        
        // Vérifier que c'est un fichier CSV
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Le fichier doit être au format CSV");
        }
        
        // Lire le contenu du fichier CSV
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        
        // Lire l'en-tête (première ligne)
        String headerLine = reader.readLine();
        if (headerLine == null) {
            throw new IllegalArgumentException("Le fichier CSV est vide");
        }
        
        // Créer un nouveau dataset
        DataSet dataSet = new DataSet();
        dataSet.setNom(originalFilename.substring(0, originalFilename.lastIndexOf(".")));
        dataSet.setDescription("Dataset importé le " + LocalDate.now());
        
        // Sauvegarder le dataset
        DataSet savedDataSet = dataSetRepository.save(dataSet);
        
        // Traiter les données du CSV
        List<Classes> classesList = new ArrayList<>();
        Set<String> uniqueClasses = new HashSet<>();
        
        // Lire les lignes de données
        while ((line = reader.readLine()) != null) {
            String[] data = line.split(",");
            
            // Supposons que la dernière colonne contient la classe
            if (data.length > 0) {
                String className = data[data.length - 1].trim();
                if (!className.isEmpty() && !uniqueClasses.contains(className)) {
                    uniqueClasses.add(className);
                    
                    Classes classEntity = new Classes();
                    classEntity.setNomClasse(className);
                    classEntity.setDataSet(savedDataSet);
                    classesList.add(classEntity);
                }
            }
        }
        
        // Sauvegarder les classes
        if (!classesList.isEmpty()) {
            classesRepository.saveAll(classesList);
            savedDataSet.setClasses(classesList);
        }
        
        return savedDataSet;
    }
    
    @Override
    public List<DataSet> getAllDataSets() {
        return dataSetRepository.findAll();
    }
    
    @Override
    public DataSet getDataSetById(int id) {
        return dataSetRepository.findById(id).orElse(null);
    }
    
    @Override
    public DataSet saveDataSet(DataSet dataSet) {
        return dataSetRepository.save(dataSet);
    }
    
    @Override
    @Transactional
    public void deleteDataSet(int id) {
        DataSet dataSet = dataSetRepository.findById(id).orElse(null);
        if (dataSet != null) {
            // Supprimer les classes associées
            if (dataSet.getClasses() != null) {
                for (Classes classe : dataSet.getClasses()) {
                    classesRepository.delete(classe);
                }
            }
            
            // Supprimer le dataset
            dataSetRepository.delete(dataSet);
        }
    }
}
