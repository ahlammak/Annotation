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

    public int calculateDataSetProgress(DataSet dataSet) {
        if (dataSet == null) {
            return 0;
        }

        List<Tache> taches = tacheRepository.findByData(dataSet);
        if (taches == null || taches.isEmpty()) {
            return 0;
        }

        int totalCouples = 0;
        int annotatedCouples = 0;

        for (Tache tache : taches) {
            List<coupleTexte> couples = tache.getCoupleTexte();
            if (couples != null) {
                totalCouples += couples.size();
                for (coupleTexte couple : couples) {
                    if (couple.getAnnotation() != null) {
                        annotatedCouples++;
                    }
                }
            }
        }
        if (totalCouples == 0) {
            return 0;
        }
        return (int) Math.round((double) annotatedCouples / totalCouples * 100);
    }
    public Map<Integer, Integer> calculateAnnotateurProgress(DataSet dataSet) {
        Map<Integer, Integer> progressByAnnotateur = new HashMap<>();
        
        if (dataSet == null) {
            return progressByAnnotateur;
        }

        List<Tache> taches = tacheRepository.findByData(dataSet);
        if (taches == null || taches.isEmpty()) {
            return progressByAnnotateur;
        }

        for (Tache tache : taches) {
            Annotateur annotateur = tache.getAnnotateur();
            if (annotateur == null) {
                continue;
            }

            int annotateurId = annotateur.getID();
            List<coupleTexte> couples = tache.getCoupleTexte();
            
            if (couples == null || couples.isEmpty()) {
                continue;
            }

            int totalCouples = couples.size();
            int annotatedCouples = 0;
            
            for (coupleTexte couple : couples) {
                if (couple.getAnnotation() != null) {
                    annotatedCouples++;
                }
            }

            int progress = (int) Math.round((double) annotatedCouples / totalCouples * 100);

            if (progressByAnnotateur.containsKey(annotateurId)) {
                int currentProgress = progressByAnnotateur.get(annotateurId);
                progressByAnnotateur.put(annotateurId, (currentProgress + progress) / 2);
            } else {
                progressByAnnotateur.put(annotateurId, progress);
            }
        }

        return progressByAnnotateur;
    }

    public Map<String, Object> calculateDataSetStatistics(DataSet dataSet) {
        Map<String, Object> statistics = new HashMap<>();
        
        if (dataSet == null) {
            return statistics;
        }

        List<Tache> taches = tacheRepository.findByData(dataSet);
        if (taches == null || taches.isEmpty()) {
            statistics.put("totalTaches", 0);
            statistics.put("totalCouples", 0);
            statistics.put("annotatedCouples", 0);
            statistics.put("progressPercentage", 0);
            statistics.put("totalAnnotateurs", 0);
            return statistics;
        }

        int totalCouples = 0;
        int annotatedCouples = 0;
        Set<Integer> uniqueAnnotateurs = new java.util.HashSet<>();

        for (Tache tache : taches) {
            if (tache.getAnnotateur() != null) {
                uniqueAnnotateurs.add(tache.getAnnotateur().getID());
            }

            List<coupleTexte> couples = tache.getCoupleTexte();
            if (couples != null) {
                totalCouples += couples.size();
                for (coupleTexte couple : couples) {
                    if (couple.getAnnotation() != null) {
                        annotatedCouples++;
                    }
                }
            }
        }

        int progressPercentage = totalCouples > 0 ? (int) Math.round((double) annotatedCouples / totalCouples * 100) : 0;

        statistics.put("totalTaches", taches.size());
        statistics.put("totalCouples", totalCouples);
        statistics.put("annotatedCouples", annotatedCouples);
        statistics.put("progressPercentage", progressPercentage);
        statistics.put("totalAnnotateurs", uniqueAnnotateurs.size());

        return statistics;
    }
}
