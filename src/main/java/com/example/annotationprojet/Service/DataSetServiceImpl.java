package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.Classes;
import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.entities.Tache;
import com.example.annotationprojet.entities.coupleTexte;
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
    @Transactional
    public void deleteDataSet(Integer id) {
        try {
            // Récupérer le dataset avec une requête distincte pour éviter les problèmes de cache
            DataSet dataSet = dataSetRepository.findById(id).orElse(null);
            if (dataSet == null) {
                return;
            }

            // 0. Désactiver temporairement les contraintes de clé étrangère
            // Cette approche est utilisée uniquement pour résoudre les problèmes de suppression
            // en production, il serait préférable de restructurer les relations entre entités
            try {
                entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=0").executeUpdate();
                System.out.println("Contraintes de clé étrangère désactivées");
            } catch (Exception e) {
                System.err.println("Erreur lors de la désactivation des contraintes de clé étrangère: " + e.getMessage());
            }

            try {
                // 1. Récupérer et supprimer les tâches associées au dataset
                List<Tache> taches = tacheRepository.findByData(dataSet);
                if (taches != null && !taches.isEmpty()) {
                    System.out.println("Suppression de " + taches.size() + " tâches");

                    // Pour chaque tâche, dissocier les couples de textes
                    for (Tache tache : taches) {
                        List<coupleTexte> couples = tache.getCoupleTexte();
                        if (couples != null) {
                            for (coupleTexte couple : new ArrayList<>(couples)) {
                                couple.setTache(null);
                                coupleTexteRepository.save(couple);
                            }
                            // Vider la liste des couples de textes de la tâche
                            couples.clear();
                        }
                    }

                    // Sauvegarder les changements avant de supprimer
                    coupleTexteRepository.flush();

                    // Supprimer les tâches
                    tacheRepository.deleteAll(taches);
                    tacheRepository.flush();
                }

                // Vérifier la structure de la table couple_texte
                try {
                    System.out.println("Vérification de la structure de la table couple_texte...");
                    List<Object[]> columns = entityManager.createNativeQuery(
                            "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                            "WHERE TABLE_SCHEMA = 'annotation-db' AND TABLE_NAME = 'couple_texte'").getResultList();

                    System.out.println("Colonnes de la table couple_texte:");
                    for (Object[] column : columns) {
                        System.out.println("- " + column[0] + " (" + column[1] + ")");
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la vérification de la structure: " + e.getMessage());
                }

                // 2. Supprimer les couples de textes associés au dataset
                // Utiliser une requête SQL native pour supprimer directement les couples de textes
                // Utiliser des paramètres positionnels (?) au lieu de paramètres nommés
                System.out.println("Tentative de suppression des couples de textes pour le dataset ID: " + dataSet.getID());
                try {
                    // Essayer avec data_set_id
                    int deletedRows = entityManager.createNativeQuery("DELETE FROM couple_texte WHERE data_set_id = ?", Integer.class)
                      .setParameter(1, dataSet.getID())
                      .executeUpdate();
                    System.out.println("Nombre de couples de textes supprimés avec data_set_id: " + deletedRows);

                    if (deletedRows == 0) {
                        // Si aucune ligne n'a été supprimée, essayer avec dataset_id
                        deletedRows = entityManager.createNativeQuery("DELETE FROM couple_texte WHERE dataset_id = ?", Integer.class)
                          .setParameter(1, dataSet.getID())
                          .executeUpdate();
                        System.out.println("Nombre de couples de textes supprimés avec dataset_id: " + deletedRows);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la suppression des couples de textes: " + e.getMessage());
                    e.printStackTrace();

                    // Essayer une approche alternative avec JDBC direct
                    try {
                        // Essayer de supprimer tous les couples de textes liés au dataset
                        // en utilisant une requête plus simple sans condition
                        String sql = "DELETE ct FROM couple_texte ct JOIN data_set ds ON ct.data_set_id = ds.id WHERE ds.id = " + dataSet.getID();
                        System.out.println("Tentative avec SQL JOIN: " + sql);
                        int result = entityManager.createNativeQuery(sql).executeUpdate();
                        System.out.println("Résultat de la suppression avec JOIN: " + result);

                        if (result == 0) {
                            // Essayer une autre approche
                            sql = "DELETE FROM couple_texte WHERE data_set_id = " + dataSet.getID();
                            System.out.println("Tentative avec SQL direct: " + sql);
                            result = entityManager.createNativeQuery(sql).executeUpdate();
                            System.out.println("Résultat de la suppression directe: " + result);
                        }
                    } catch (Exception ex) {
                        System.err.println("Erreur lors de la tentative alternative: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }

                // 3. Supprimer les classes associées au dataset
                // Utiliser une requête SQL native pour supprimer directement les classes
                try {
                    int deletedClasses = entityManager.createNativeQuery("DELETE FROM classes WHERE data_set_id = ?", Integer.class)
                      .setParameter(1, dataSet.getID())
                      .executeUpdate();
                    System.out.println("Nombre de classes supprimées: " + deletedClasses);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la suppression des classes: " + e.getMessage());
                    e.printStackTrace();

                    // Essayer une approche alternative
                    try {
                        String sql = "DELETE FROM classes WHERE data_set_id = " + dataSet.getID();
                        System.out.println("Tentative avec SQL direct pour les classes: " + sql);
                        int result = entityManager.createNativeQuery(sql).executeUpdate();
                        System.out.println("Résultat de la suppression directe des classes: " + result);
                    } catch (Exception ex) {
                        System.err.println("Erreur lors de la tentative alternative pour les classes: " + ex.getMessage());
                    }
                }

                // 4. Supprimer le dataset lui-même
                System.out.println("Suppression du dataset avec ID: " + dataSet.getID());
                try {
                    int deletedDataset = entityManager.createNativeQuery("DELETE FROM data_set WHERE id = ?", Integer.class)
                      .setParameter(1, dataSet.getID())
                      .executeUpdate();
                    System.out.println("Dataset supprimé: " + (deletedDataset > 0));
                } catch (Exception e) {
                    System.err.println("Erreur lors de la suppression du dataset: " + e.getMessage());
                    e.printStackTrace();

                    // Essayer une approche alternative
                    try {
                        String sql = "DELETE FROM data_set WHERE id = " + dataSet.getID();
                        System.out.println("Tentative avec SQL direct pour le dataset: " + sql);
                        int result = entityManager.createNativeQuery(sql).executeUpdate();
                        System.out.println("Résultat de la suppression directe du dataset: " + result);
                    } catch (Exception ex) {
                        System.err.println("Erreur lors de la tentative alternative pour le dataset: " + ex.getMessage());
                    }
                }

                // 5. Supprimer le fichier physique si nécessaire
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
            } finally {
                // Réactiver les contraintes de clé étrangère
                try {
                    entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS=1").executeUpdate();
                    System.out.println("Contraintes de clé étrangère réactivées");
                } catch (Exception e) {
                    System.err.println("Erreur lors de la réactivation des contraintes de clé étrangère: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du dataset: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du dataset : " + e.getMessage(), e);
        }
    }
}
