package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.entities.Tache;
import com.example.annotationprojet.entities.coupleTexte;
import com.example.annotationprojet.repositories.TacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProgressService {

    @Autowired
    private TacheRepository tacheRepository;


    private class DatasetStats {
        int totalCouples = 0;
        int annotatedCouples = 0;
        Set<Integer> uniqueAnnotateurs = new java.util.HashSet<>();
        Map<Integer, AnnotateurStats> annotateurStatsMap = new HashMap<>();


        int getProgressPercentage() {
            return totalCouples > 0 ? (int) Math.round((double) annotatedCouples / totalCouples * 100) : 0;
        }
    }


    private class AnnotateurStats {
        int totalCouples = 0;
        int annotatedCouples = 0;


        int getProgressPercentage() {
            return totalCouples > 0 ? (int) Math.round((double) annotatedCouples / totalCouples * 100) : 0;
        }


        void merge(AnnotateurStats other) {
            int thisProgress = getProgressPercentage();
            int otherProgress = other.getProgressPercentage();

            // Moyenne pondérée des pourcentages de progression
            totalCouples += other.totalCouples;
            annotatedCouples += other.annotatedCouples;
        }
    }


    private DatasetStats collectDatasetStats(DataSet dataSet) {
        DatasetStats stats = new DatasetStats();

        if (dataSet == null) {
            return stats;
        }

        List<Tache> taches = tacheRepository.findByData(dataSet);
        if (taches == null || taches.isEmpty()) {
            return stats;
        }

        for (Tache tache : taches) {
            // Collecter les annotateurs uniques
            Annotateur annotateur = tache.getAnnotateur();
            if (annotateur != null) {
                int annotateurId = annotateur.getID();
                stats.uniqueAnnotateurs.add(annotateurId);

                // Initialiser les statistiques de l'annotateur si nécessaire
                if (!stats.annotateurStatsMap.containsKey(annotateurId)) {
                    stats.annotateurStatsMap.put(annotateurId, new AnnotateurStats());
                }
            }

            // Collecter les statistiques des couples de texte
            List<coupleTexte> couples = tache.getCoupleTexte();
            if (couples != null) {
                stats.totalCouples += couples.size();

                for (coupleTexte couple : couples) {
                    if (couple.getAnnotation() != null) {
                        stats.annotatedCouples++;

                        // Mettre à jour les statistiques de l'annotateur
                        if (annotateur != null) {
                            AnnotateurStats annotateurStats = stats.annotateurStatsMap.get(annotateur.getID());
                            annotateurStats.annotatedCouples++;
                        }
                    }
                }

                // Mettre à jour le nombre total de couples pour l'annotateur
                if (annotateur != null && couples.size() > 0) {
                    AnnotateurStats annotateurStats = stats.annotateurStatsMap.get(annotateur.getID());
                    annotateurStats.totalCouples += couples.size();
                }
            }
        }

        return stats;
    }

    public int calculateDataSetProgress(DataSet dataSet) {
        return collectDatasetStats(dataSet).getProgressPercentage();
    }


    public Map<Integer, Integer> calculateAnnotateurProgress(DataSet dataSet) {
        DatasetStats stats = collectDatasetStats(dataSet);
        Map<Integer, Integer> progressByAnnotateur = new HashMap<>();

        for (Map.Entry<Integer, AnnotateurStats> entry : stats.annotateurStatsMap.entrySet()) {
            progressByAnnotateur.put(entry.getKey(), entry.getValue().getProgressPercentage());
        }

        return progressByAnnotateur;
    }


    public Map<String, Object> calculateDataSetStatistics(DataSet dataSet) {
        DatasetStats stats = collectDatasetStats(dataSet);
        Map<String, Object> statistics = new HashMap<>();

        // Nombre de tâches
        List<Tache> taches = dataSet != null ? tacheRepository.findByData(dataSet) : null;
        int tachesCount = (taches != null) ? taches.size() : 0;

        statistics.put("totalTaches", tachesCount);
        statistics.put("totalCouples", stats.totalCouples);
        statistics.put("annotatedCouples", stats.annotatedCouples);
        statistics.put("progressPercentage", stats.getProgressPercentage());
        statistics.put("totalAnnotateurs", stats.uniqueAnnotateurs.size());

        return statistics;
    }
}
