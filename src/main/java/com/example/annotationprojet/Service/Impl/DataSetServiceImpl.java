package com.example.annotationprojet.Service.Impl;

import com.example.annotationprojet.Service.DataSetService;
import com.example.annotationprojet.entities.Classes;
import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.entities.Tache;
import com.example.annotationprojet.entities.coupleTexte;
import com.example.annotationprojet.repositories.AnnotationRepository;
import com.example.annotationprojet.repositories.ClassesRepository;
import com.example.annotationprojet.repositories.DataSetRepository;
import com.example.annotationprojet.repositories.TacheRepository;
import com.example.annotationprojet.repositories.coupleTexteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;

@Service
public class DataSetServiceImpl implements DataSetService {

    private static final String UPLOAD_DIR = "uploads";

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private coupleTexteRepository coupleTexteRepository;

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private AnnotationRepository annotationRepository;

    @PersistenceContext
    private EntityManager entityManager;



    @Override
    @Transactional
    public DataSet importDataSet(MultipartFile file, String nom, String description, String classes) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Le fichier est vide");
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) throw new IllegalArgumentException("Nom de fichier invalide");

        String extension = getFileExtension(originalFilename);
        if (!List.of("csv", "xlsx", "xls").contains(extension)) {
            throw new IllegalArgumentException("Format de fichier non supporté. Seuls les formats CSV et Excel (.xlsx, .xls) sont acceptés.");
        }

        System.out.println("Début de l'importation du fichier: " + originalFilename);
        Path filePath = saveFile(file);
        System.out.println("Fichier sauvegardé à: " + filePath);

        DataSet dataSet = createDataSet(nom, description, filePath);
        processClasses(classes, dataSet);
        System.out.println("Dataset créé avec ID: " + dataSet.getID());


        System.out.println("Dataset créé avec succès. Le traitement des données sera effectué en arrière-plan.");
        return dataSet;
    }

    @Override
    @Transactional
    public DataSet importDataSetWithProcessing(MultipartFile file, String nom, String description, String classes) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Le fichier est vide");
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) throw new IllegalArgumentException("Nom de fichier invalide");

        String extension = getFileExtension(originalFilename);
        if (!List.of("csv", "xlsx", "xls").contains(extension)) {
            throw new IllegalArgumentException("Format de fichier non supporté. Seuls les formats CSV et Excel (.xlsx, .xls) sont acceptés.");
        }

        System.out.println("[SYNC] Début de l'importation directe du fichier: " + originalFilename);
        Path filePath = saveFile(file);
        System.out.println("[SYNC] Fichier sauvegardé à: " + filePath);

        DataSet dataSet = createDataSet(nom, description, filePath);
        processClasses(classes, dataSet);
        System.out.println("[SYNC] Dataset créé avec ID: " + dataSet.getID());

        // Traitement DIRECT et SYNCHRONE des données (comme avant)
        try {
            processDataSetFile(dataSet);
            System.out.println("[SYNC] Traitement terminé avec succès pour: " + nom);
        } catch (Exception e) {
            System.err.println("[SYNC] Erreur lors du traitement: " + e.getMessage());
            // On garde le dataset même si le traitement échoue
        }

        return dataSet;
    }

    private Path saveFile(MultipartFile file) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR, file.getOriginalFilename());
        Files.createDirectories(filePath.getParent());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filePath;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private DataSet createDataSet(String nom, String description, Path filePath) {
        DataSet dataSet = new DataSet();
        dataSet.setNom(nom);
        dataSet.setDescription(description);
        dataSet.setUrl(filePath.toString());
        return dataSetRepository.save(dataSet);
    }

    private void processClasses(String classes, DataSet dataSet) {
        if (classes == null || classes.isEmpty()) return;
        String[] classArray = classes.split(",");
        for (String className : classArray) {
            if (!className.trim().isEmpty()) {
                Classes cls = new Classes();
                cls.setNomClasse(className.trim());
                cls.setDataSet(dataSet);
                classesRepository.save(cls);
            }
        }
    }

    @Override
    public void processDataSetFile(DataSet dataset) throws IOException {
        final int MAX_ROWS = 500; // Limite à 500 lignes

        String filePath = dataset.getUrl();
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Chemin de fichier non défini");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Fichier introuvable: " + filePath);
        }

        List<coupleTexte> coupleTextes = new ArrayList<>();
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();

        System.out.println("Importation limitée à " + MAX_ROWS + " lignes maximum.");

        if ("csv".equals(extension)) {
            processCsvFile(file, dataset, coupleTextes, MAX_ROWS);
        } else if ("xlsx".equals(extension) || "xls".equals(extension)) {
            processExcelFile(file, dataset, coupleTextes, MAX_ROWS);
        } else {
            throw new IllegalArgumentException("Format de fichier non supporté: " + extension);
        }

        if (!coupleTextes.isEmpty()) {
            coupleTexteRepository.saveAll(coupleTextes);
            System.out.println("Sauvegarde finale de " + coupleTextes.size() + " couples de textes.");
        }
    }

    /**
     * Interface pour les différents lecteurs de fichiers
     */
    private interface FileReader {
        List<String[]> readFile(File file, int maxRows) throws IOException;
    }

    /**
     * Lecteur de fichiers CSV
     */
    private class CsvFileReader implements FileReader {
        @Override
        public List<String[]> readFile(File file, int maxRows) throws IOException {
            List<String[]> rows = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
                String line;
                boolean firstLine = true;

                // Déterminer le séparateur
                String separator = ",";
                String headerLine = reader.readLine();
                if (headerLine != null) {
                    if (headerLine.contains(";")) separator = ";";
                    else if (headerLine.contains("\t")) separator = "\t";
                }

                // Lire les lignes
                while ((line = reader.readLine()) != null && rows.size() < maxRows) {
                    if (line.trim().isEmpty()) continue;

                    String[] columns = line.split(separator);
                    if (columns.length >= 2) {
                        rows.add(new String[] { columns[0].trim(), columns[1].trim() });
                    }
                }

                // Vérifier s'il reste des lignes non importées
                if (reader.readLine() != null) {
                    System.out.println("Limite de " + maxRows + " lignes atteinte. Les lignes restantes ne seront pas importées.");
                }

                System.out.println("Lu " + rows.size() + " lignes depuis le fichier CSV.");
            }
            return rows;
        }
    }

    /**
     * Lecteur de fichiers Excel
     */
    private class ExcelFileReader implements FileReader {
        @Override
        public List<String[]> readFile(File file, int maxRows) throws IOException {
            List<String[]> rows = new ArrayList<>();
            try (InputStream is = new FileInputStream(file);
                 Workbook workbook = WorkbookFactory.create(is)) {

                Sheet sheet = workbook.getSheetAt(0);
                boolean firstRow = true;
                int totalRows = sheet.getPhysicalNumberOfRows();

                for (Row row : sheet) {
                    // Ignorer l'en-tête
                    if (firstRow) {
                        firstRow = false;
                        continue;
                    }

                    // Vérifier si on a atteint la limite
                    if (rows.size() >= maxRows) {
                        System.out.println("Limite de " + maxRows + " lignes atteinte. " + (totalRows - maxRows - 1) + " lignes restantes ne seront pas importées.");
                        break;
                    }

                    Cell cell1 = row.getCell(0);
                    Cell cell2 = row.getCell(1);

                    if (cell1 != null && cell2 != null) {
                        String texte1 = getCellValueAsString(cell1);
                        String texte2 = getCellValueAsString(cell2);

                        if (!texte1.trim().isEmpty() && !texte2.trim().isEmpty()) {
                            rows.add(new String[] { texte1, texte2 });
                        }
                    }
                }

                System.out.println("Lu " + rows.size() + " lignes depuis le fichier Excel.");
            }
            return rows;
        }
    }

    /**
     * Traite un fichier CSV
     */
    private void processCsvFile(File file, DataSet dataset, List<coupleTexte> coupleTextes, int maxRows) throws IOException {
        processFile(file, dataset, coupleTextes, maxRows, new CsvFileReader());
    }

    /**
     * Traite un fichier Excel
     */
    private void processExcelFile(File file, DataSet dataset, List<coupleTexte> coupleTextes, int maxRows) throws IOException {
        processFile(file, dataset, coupleTextes, maxRows, new ExcelFileReader());
    }

    /**
     * Méthode générique pour traiter un fichier
     */
    private void processFile(File file, DataSet dataset, List<coupleTexte> coupleTextes, int maxRows, FileReader fileReader) throws IOException {
        List<String[]> rows = fileReader.readFile(file, maxRows);

        for (String[] row : rows) {
            String texte1 = row[0];
            String texte2 = row[1];

            coupleTexte couple = new coupleTexte();
            couple.setTexte1(texte1);
            couple.setTexte2(texte2);
            couple.setDataSet(dataset);
            coupleTextes.add(couple);
        }

        System.out.println("Importé " + rows.size() + " couples de textes.");
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    @Override
    public List<DataSet> getAllDataSets() {
        return dataSetRepository.findAll();
    }

    @Override
    public DataSet getDataSetById(Integer id) {
        return dataSetRepository.findById(id).orElse(null);
    }

    @Override
    public DataSet saveDataSet(DataSet dataSet) {
        return dataSetRepository.save(dataSet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataSet(Integer id) {
        try {
            System.out.println("Début de la suppression du dataset avec ID: " + id);

            // Récupérer le dataset
            DataSet dataSet = dataSetRepository.findById(id).orElse(null);
            if (dataSet == null) {
                System.out.println("Dataset non trouvé avec ID: " + id);
                throw new RuntimeException("Dataset non trouvé avec ID: " + id);
            }

            System.out.println("Dataset trouvé: " + dataSet.getNom());

            // 1. Supprimer les annotations liées aux couples de texte du dataset
            System.out.println("Suppression des annotations...");
            List<coupleTexte> couples = coupleTexteRepository.findByDataSet(dataSet);
            for (coupleTexte couple : couples) {
                if (couple.getAnnotation() != null) {
                    annotationRepository.delete(couple.getAnnotation());
                    couple.setAnnotation(null);
                }
            }

            // 2. Supprimer les tâches associées au dataset
            System.out.println("Suppression des tâches...");
            List<Tache> taches = tacheRepository.findByData(dataSet);
            if (taches != null && !taches.isEmpty()) {
                System.out.println("Suppression de " + taches.size() + " tâches");

                // Dissocier les couples de texte des tâches
                for (Tache tache : taches) {
                    List<coupleTexte> couplesTask = tache.getCoupleTexte();
                    if (couplesTask != null) {
                        for (coupleTexte couple : couplesTask) {
                            couple.setTache(null);
                        }
                    }
                }

                // Supprimer les tâches
                tacheRepository.deleteAll(taches);
            }

            // 3. Supprimer les couples de textes associés au dataset
            System.out.println("Suppression des couples de textes...");
            coupleTexteRepository.deleteByDataSet(dataSet);

            // 4. Supprimer les classes associées au dataset
            System.out.println("Suppression des classes...");
            List<Classes> classes = classesRepository.findByDataSet(dataSet);
            if (classes != null && !classes.isEmpty()) {
                classesRepository.deleteAll(classes);
            }

            // 5. Supprimer le dataset lui-même
            System.out.println("Suppression du dataset avec ID: " + dataSet.getID());
            dataSetRepository.delete(dataSet);

            // 6. Supprimer le fichier physique si nécessaire
            String filePath = dataSet.getUrl();
            if (filePath != null && !filePath.isEmpty()) {
                try {
                    File file = new File(filePath);
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        System.out.println("Fichier supprimé: " + deleted);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la suppression du fichier : " + e.getMessage());
                }
            }

            System.out.println("Dataset supprimé avec succès: " + dataSet.getNom());
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du dataset: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du dataset : " + e.getMessage(), e);
        }
    }
}
