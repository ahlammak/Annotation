package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.entities.Tache;
import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.coupleTexte;
import com.example.annotationprojet.repositories.DataSetRepository;
import com.example.annotationprojet.repositories.TacheRepository;
import com.example.annotationprojet.repositories.AnnotateurRepository;
import com.example.annotationprojet.repositories.coupleTexteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Service
public class AffectationAnnotateurService {

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @Autowired
    private coupleTexteRepository coupleTexteRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    /**
     * Affecte des tâches aléatoirement aux annotateurs pour un dataset donné
     * @param datasetId L'ID du dataset
     * @param annotateurIds Les IDs des annotateurs
     * @return Le nombre de tâches créées
     */
    @Transactional
    public int affecterTachesAleatoires(Integer datasetId, List<Integer> annotateurIds) {
        // Utiliser la date actuelle comme date limite par défaut, mais sans l'heure
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return affecterTachesAleatoires(datasetId, annotateurIds, calendar.getTime());
    }

    /**
     * Affecte des tâches aléatoirement aux annotateurs pour un dataset donné avec une date limite spécifiée
     * @param datasetId L'ID du dataset
     * @param annotateurIds Les IDs des annotateurs
     * @param dateLimite La date limite pour les tâches
     * @return Le nombre de tâches créées
     */
    @Transactional
    public int affecterTachesAleatoires(Integer datasetId, List<Integer> annotateurIds, Date dateLimite) {
        // S'assurer que la date limite n'a pas d'heure, de minutes ou de secondes
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateLimite);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dateLimite = calendar.getTime();
        if (annotateurIds == null || annotateurIds.isEmpty()) {
            throw new IllegalArgumentException("Aucun annotateur sélectionné");
        }

        DataSet dataset = dataSetRepository.findById(datasetId)
                .orElseThrow(() -> new IllegalArgumentException("Dataset introuvable avec l'ID: " + datasetId));

        List<coupleTexte> coupleTextes = coupleTexteRepository.findByDataSet(dataset);

        if (coupleTextes.isEmpty()) {
            throw new IllegalArgumentException("Le dataset ne contient aucun couple de texte");
        }

        Collections.shuffle(coupleTextes);

        List<Annotateur> annotateurs = annotateurRepository.findAllById(annotateurIds);

        if (annotateurs.isEmpty()) {
            throw new IllegalArgumentException("Aucun annotateur trouvé avec les IDs fournis");
        }

        int totalCouples = coupleTextes.size();

        int nombreAnnotateurs = annotateurs.size();


        List<Tache> tachesCreees = new ArrayList<>();

        if (totalCouples <= nombreAnnotateurs) {

            for (int i = 0; i < totalCouples; i++) {
                Tache tache = new Tache();
                tache.setData(dataset);
                tache.setAnnotateur(annotateurs.get(i));

                List<coupleTexte> couplesForTache = new ArrayList<>();
                coupleTexte couple = coupleTextes.get(i);
                couplesForTache.add(couple);
                tache.setCoupleTexte(couplesForTache);
                tache.setDateLimite(dateLimite);

                Tache savedTache = tacheRepository.save(tache);

                couple.setTache(savedTache);
                coupleTexteRepository.save(couple);

                tachesCreees.add(savedTache);
            }
        } else {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < totalCouples; i++) {
                indices.add(i);
            }
            Collections.shuffle(indices);
            int minCouplesPerAnnotateur = totalCouples / nombreAnnotateurs;

            int extraCouples = totalCouples % nombreAnnotateurs;

            int currentIndex = 0;


            for (int i = 0; i < nombreAnnotateurs; i++) {

                int couplesForThisAnnotateur = minCouplesPerAnnotateur + (i < extraCouples ? 1 : 0);

                if (couplesForThisAnnotateur > 0) {

                    Tache tache = new Tache();
                    tache.setData(dataset);
                    tache.setAnnotateur(annotateurs.get(i));


                    List<coupleTexte> couplesForTache = new ArrayList<>();
                    List<coupleTexte> couplesToUpdate = new ArrayList<>();

                    for (int j = 0; j < couplesForThisAnnotateur; j++) {
                        if (currentIndex < indices.size()) {
                            coupleTexte couple = coupleTextes.get(indices.get(currentIndex));
                            couplesForTache.add(couple);
                            couplesToUpdate.add(couple);
                            currentIndex++;
                        }
                    }

                    tache.setCoupleTexte(couplesForTache);
                    tache.setDateLimite(dateLimite);


                    Tache savedTache = tacheRepository.save(tache);


                    for (coupleTexte couple : couplesToUpdate) {
                        couple.setTache(savedTache);
                    }
                    coupleTexteRepository.saveAll(couplesToUpdate);

                    tachesCreees.add(savedTache);
                }
            }
        }

        return tachesCreees.size();
    }

    /**
     * Réaffecte les tâches d'un annotateur supprimé aux autres annotateurs du même dataset
     * @param dataset Le dataset concerné
     * @param annotateurToRemove L'annotateur à supprimer
     * @return Le nombre de tâches réaffectées
     */
    @Transactional
    public int reassignTasks(DataSet dataset, Annotateur annotateurToRemove) {
        try {
            // Récupérer les tâches de l'annotateur pour ce dataset
            List<Tache> tachesToReassign = tacheRepository.findByDataAndAnnotateur(dataset, annotateurToRemove);

            if (tachesToReassign.isEmpty()) {
                return 0; // Aucune tâche à réaffecter
            }

            // Récupérer tous les autres annotateurs qui ont déjà des tâches pour ce dataset
            List<Tache> allTaches = tacheRepository.findByData(dataset);
            List<Annotateur> otherAnnotateurs = new ArrayList<>();

            for (Tache tache : allTaches) {
                if (tache.getAnnotateur() != null && !tache.getAnnotateur().getID().equals(annotateurToRemove.getID())
                        && !otherAnnotateurs.contains(tache.getAnnotateur())) {
                    otherAnnotateurs.add(tache.getAnnotateur());
                }
            }

            if (otherAnnotateurs.isEmpty()) {
                return 0; // Aucun autre annotateur disponible pour réaffecter les tâches
            }

            int reassignedCount = 0;

            // Préparer une map des tâches existantes par annotateur
            Map<Integer, Tache> existingTachesByAnnotateur = new HashMap<>();
            for (Annotateur annotateur : otherAnnotateurs) {
                List<Tache> annotTaches = tacheRepository.findByDataAndAnnotateur(dataset, annotateur);
                if (!annotTaches.isEmpty()) {
                    existingTachesByAnnotateur.put(annotateur.getID(), annotTaches.get(0));
                }
            }

            // Collecter tous les couples non annotés de toutes les tâches
            List<coupleTexte> allUnannotatedCouples = new ArrayList<>();
            for (Tache tache : tachesToReassign) {
                List<coupleTexte> couples = tache.getCoupleTexte();
                if (couples != null) {
                    for (coupleTexte couple : couples) {
                        if (couple.getAnnotation() == null) {
                            allUnannotatedCouples.add(couple);
                            // Détacher le couple de sa tâche actuelle
                            couple.setTache(null);
                        }
                    }
                }
            }

            if (allUnannotatedCouples.isEmpty()) {
                return 0; // Aucun couple non annoté à réaffecter
            }

            // Sauvegarder les couples détachés
            coupleTexteRepository.saveAll(allUnannotatedCouples);

            // Répartir les couples non annotés entre les autres annotateurs
            Collections.shuffle(allUnannotatedCouples); // Mélanger pour une distribution aléatoire
            Collections.shuffle(otherAnnotateurs); // Mélanger les annotateurs aussi

            int couplesPerAnnotateur = Math.max(1, allUnannotatedCouples.size() / otherAnnotateurs.size());

            // Créer ou récupérer les tâches pour chaque annotateur
            Map<Integer, Tache> targetTaches = new HashMap<>();
            for (Annotateur annotateur : otherAnnotateurs) {
                Tache targetTache;
                if (existingTachesByAnnotateur.containsKey(annotateur.getID())) {
                    targetTache = existingTachesByAnnotateur.get(annotateur.getID());
                } else {
                    targetTache = new Tache();
                    targetTache.setData(dataset);
                    targetTache.setAnnotateur(annotateur);
                    // Utiliser la date limite de la première tâche à réaffecter
                    targetTache.setDateLimite(tachesToReassign.get(0).getDateLimite());
                    targetTache.setCoupleTexte(new ArrayList<>());
                    targetTache = tacheRepository.save(targetTache);
                }
                targetTaches.put(annotateur.getID(), targetTache);
            }

            // Distribuer les couples aux annotateurs
            int currentAnnotateurIndex = 0;
            for (int i = 0; i < allUnannotatedCouples.size(); i++) {
                coupleTexte couple = allUnannotatedCouples.get(i);

                // Changer d'annotateur tous les couplesPerAnnotateur couples
                if (i > 0 && i % couplesPerAnnotateur == 0) {
                    currentAnnotateurIndex = (currentAnnotateurIndex + 1) % otherAnnotateurs.size();
                }

                Annotateur annotateur = otherAnnotateurs.get(currentAnnotateurIndex);
                Tache targetTache = targetTaches.get(annotateur.getID());

                // Associer le couple à la tâche cible
                couple.setTache(targetTache);
                coupleTexteRepository.save(couple);

                reassignedCount++;
            }

            // Supprimer les tâches de l'annotateur supprimé
            // D'abord, détacher tous les couples annotés
            for (Tache tache : tachesToReassign) {
                List<coupleTexte> annotatedCouples = new ArrayList<>();
                if (tache.getCoupleTexte() != null) {
                    for (coupleTexte couple : tache.getCoupleTexte()) {
                        if (couple.getAnnotation() != null) {
                            annotatedCouples.add(couple);
                        }
                    }
                }

                // Détacher les couples annotés de la tâche
                for (coupleTexte couple : annotatedCouples) {
                    couple.setTache(null);
                    coupleTexteRepository.save(couple);
                }
            }

            // Maintenant, supprimer les tâches
            tacheRepository.deleteAll(tachesToReassign);

            return reassignedCount;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Réaffecte les tâches d'un annotateur supprimé à tous les datasets auxquels il est affecté
     * @param annotateurToRemove L'annotateur à supprimer
     * @return Le nombre total de tâches réaffectées
     */
    @Transactional
    public int reassignTasksForAllDatasets(Annotateur annotateurToRemove) {
        try {
            // Récupérer toutes les tâches de l'annotateur
            List<Tache> allTaches = tacheRepository.findByAnnotateur(annotateurToRemove);

            if (allTaches.isEmpty()) {
                return 0; // Aucune tâche à réaffecter
            }

            // Regrouper les tâches par dataset
            Map<Integer, DataSet> datasetsMap = new HashMap<>();
            for (Tache tache : allTaches) {
                if (tache.getData() != null) {
                    datasetsMap.put(tache.getData().getID(), tache.getData());
                }
            }

            // Réaffecter les tâches pour chaque dataset
            int totalReassignedCount = 0;
            for (DataSet dataset : datasetsMap.values()) {
                int reassignedCount = reassignTasks(dataset, annotateurToRemove);
                totalReassignedCount += reassignedCount;
                System.out.println("Réaffecté " + reassignedCount + " tâches pour le dataset " + dataset.getNom());
            }

            return totalReassignedCount;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
