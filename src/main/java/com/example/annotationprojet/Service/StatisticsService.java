package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.*;
import com.example.annotationprojet.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private  AnnotateurRepository   annotateurRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private coupleTexteRepository coupleTexteRepository;

    @Autowired
    private AnnotationRepository annotationRepository;

    /**
     * Calcule toutes les statistiques de l'application
     * @return Map contenant toutes les statistiques
     */
    public Map<String, Object> getAllStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // Statistiques des datasets
        statistics.put("datasets", getDatasetStatistics());

        // Statistiques des annotateurs
        statistics.put("annotateurs", getAnnotateurStatistics());

        // Statistiques des classes
        statistics.put("classes", getClassesStatistics());

        // Statistiques des tâches
        statistics.put("taches", getTacheStatistics());

        // Statistiques des annotations
        statistics.put("annotations", getAnnotationStatistics());

        return statistics;
    }

    /**
     * Calcule les statistiques des datasets
     */
    public Map<String, Object> getDatasetStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<DataSet> allDatasets = dataSetRepository.findAll();
        long totalDatasets = allDatasets.size();
        long datasetsEnTraitement = allDatasets.stream()
                .mapToLong(ds -> ds.getIsProcessing() != null && ds.getIsProcessing() ? 1 : 0)
                .sum();

        long totalCouplesTexte = coupleTexteRepository.count();

        stats.put("total", totalDatasets);
        stats.put("enTraitement", datasetsEnTraitement);
        stats.put("totalCouplesTexte", totalCouplesTexte);

        return stats;
    }

    /**
     * Calcule les statistiques des annotateurs
     */
    public Map<String, Object> getAnnotateurStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Annotateur> annotateursActifs = annotateurRepository.findByActiveTrue();
        long totalAnnotateurs = annotateursActifs.size();

        // Calculer le nombre d'annotations réalisées
        long totalAnnotations = annotationRepository.count();

        // Calculer le nombre d'annotateurs ayant au moins une tâche
        long annotateursAvecTaches = annotateursActifs.stream()
                .mapToLong(annotateur -> annotateur.getTaches() != null && !annotateur.getTaches().isEmpty() ? 1 : 0)
                .sum();

        stats.put("total", totalAnnotateurs);
        stats.put("avecTaches", annotateursAvecTaches);
        stats.put("totalAnnotations", totalAnnotations);

        return stats;
    }

    /**
     * Calcule les statistiques des classes
     */
    public Map<String, Object> getClassesStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalClasses = classesRepository.count();

        // Calculer le nombre de datasets ayant des classes
        List<DataSet> datasets = dataSetRepository.findAll();
        long datasetsAvecClasses = datasets.stream()
                .mapToLong(ds -> ds.getClasses() != null && !ds.getClasses().isEmpty() ? 1 : 0)
                .sum();

        // Calculer la moyenne de classes par dataset
        double moyenneParDataset = datasets.size() > 0 ? (double) totalClasses / datasets.size() : 0;

        stats.put("total", totalClasses);
        stats.put("datasetsAvecClasses", datasetsAvecClasses);
        stats.put("moyenneParDataset", Math.round(moyenneParDataset * 100.0) / 100.0);

        return stats;
    }

    /**
     * Calcule les statistiques des tâches
     */
    public Map<String, Object> getTacheStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Tache> allTaches = tacheRepository.findAll();
        long totalTaches = allTaches.size();

        // Calculer les tâches par statut
        long tachesTerminees = 0;
        long tachesEnCours = 0;
        long tachesNonCommencees = 0;

        for (Tache tache : allTaches) {
            String status = tache.getProgressStatus();
            switch (status) {
                case "Terminé":
                    tachesTerminees++;
                    break;
                case "En cours":
                    tachesEnCours++;
                    break;
                case "Non commencé":
                    tachesNonCommencees++;
                    break;
            }
        }

        stats.put("total", totalTaches);
        stats.put("terminees", tachesTerminees);
        stats.put("enCours", tachesEnCours);
        stats.put("nonCommencees", tachesNonCommencees);

        return stats;
    }

    /**
     * Calcule les statistiques des annotations
     */
    public Map<String, Object> getAnnotationStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalAnnotations = annotationRepository.count();
        long totalCouplesTexte = coupleTexteRepository.count();

        // Calculer le pourcentage d'avancement global
        double pourcentageGlobal = totalCouplesTexte > 0 ?
                (double) totalAnnotations / totalCouplesTexte * 100 : 0;

        stats.put("totalAnnotations", totalAnnotations);
        stats.put("totalCouplesTexte", totalCouplesTexte);
        stats.put("pourcentageGlobal", Math.round(pourcentageGlobal * 100.0) / 100.0);

        return stats;
    }

    /**
     * Calcule les statistiques détaillées par dataset
     */
    public Map<String, Object> getDetailedDatasetStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<DataSet> datasets = dataSetRepository.findAll();

        for (DataSet dataset : datasets) {
            Map<String, Object> datasetStats = new HashMap<>();

            // Nombre de couples de texte
            long couplesCount = dataset.getCoupleTexte() != null ? dataset.getCoupleTexte().size() : 0;

            // Nombre de tâches
            long tachesCount = dataset.getTaches() != null ? dataset.getTaches().size() : 0;

            // Nombre de classes
            long classesCount = dataset.getClasses() != null ? dataset.getClasses().size() : 0;

            // Nombre d'annotations pour ce dataset
            long annotationsCount = 0;
            if (dataset.getCoupleTexte() != null) {
                annotationsCount = dataset.getCoupleTexte().stream()
                        .mapToLong(couple -> couple.getAnnotation() != null ? 1 : 0)
                        .sum();
            }

            // Pourcentage d'avancement
            double pourcentage = couplesCount > 0 ? (double) annotationsCount / couplesCount * 100 : 0;

            datasetStats.put("nom", dataset.getNom());
            datasetStats.put("couplesTexte", couplesCount);
            datasetStats.put("taches", tachesCount);
            datasetStats.put("classes", classesCount);
            datasetStats.put("annotations", annotationsCount);
            datasetStats.put("pourcentage", Math.round(pourcentage * 100.0) / 100.0);
            datasetStats.put("enTraitement", dataset.getIsProcessing() != null && dataset.getIsProcessing());

            stats.put("dataset_" + dataset.getID(), datasetStats);
        }

        return stats;
    }

    /**
     * Calcule les statistiques spécifiques à un annotateur
     * @param annotateur L'annotateur pour lequel calculer les statistiques
     * @return Map contenant les statistiques de l'annotateur
     */
    public Map<String, Object> getAnnotateurSpecificStatistics(Annotateur annotateur) {
        Map<String, Object> stats = new HashMap<>();

        if (annotateur == null) {
            // Retourner des statistiques vides si l'annotateur est null
            stats.put("totalTaches", 0);
            stats.put("tachesTerminees", 0);
            stats.put("tachesEnCours", 0);
            stats.put("totalAnnotations", 0);
            return stats;
        }

        System.out.println("Calcul des statistiques pour l'annotateur: " + annotateur.getPrenom() + " " + annotateur.getNom() + " (ID: " + annotateur.getID() + ")");

        // Récupérer les tâches via le repository (plus sûr que l'accès lazy)
        List<Tache> taches = null;
        int totalTaches = 0;
        int tachesTerminees = 0;
        int tachesEnCours = 0;

        try {
            taches = tacheRepository.findByAnnotateur(annotateur);
            totalTaches = taches != null ? taches.size() : 0;
            System.out.println("Nombre de tâches récupérées: " + totalTaches);

            // Calculer les tâches par statut
            if (taches != null) {
                for (Tache tache : taches) {
                    String status = tache.getProgressStatus();
                    System.out.println("Tâche ID " + tache.getID() + " - Statut: " + status);
                    switch (status) {
                        case "Terminé":
                            tachesTerminees++;
                            break;
                        case "En cours":
                            tachesEnCours++;
                            break;
                        // "Non commencé" n'est pas compté séparément
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des tâches: " + e.getMessage());
            e.printStackTrace();
        }

        // Compter les annotations via le repository (plus sûr)
        int totalAnnotations = 0;
        try {
            List<Annotation> annotations = annotationRepository.findByAnnotateur(annotateur);
            totalAnnotations = annotations != null ? annotations.size() : 0;
            System.out.println("Nombre d'annotations récupérées: " + totalAnnotations);
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des annotations: " + e.getMessage());
            e.printStackTrace();
        }

        stats.put("totalTaches", totalTaches);
        stats.put("tachesTerminees", tachesTerminees);
        stats.put("tachesEnCours", tachesEnCours);
        stats.put("totalAnnotations", totalAnnotations);

        System.out.println("Statistiques calculées: " + stats);

        return stats;
    }
}
