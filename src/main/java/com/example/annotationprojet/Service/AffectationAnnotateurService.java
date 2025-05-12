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
        // Utiliser la date actuelle comme date limite par défaut
        return affecterTachesAleatoires(datasetId, annotateurIds, new Date());
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
        if (annotateurIds == null || annotateurIds.isEmpty()) {
            throw new IllegalArgumentException("Aucun annotateur sélectionné");
        }

        // Récupérer le dataset
        DataSet dataset = dataSetRepository.findById(datasetId)
                .orElseThrow(() -> new IllegalArgumentException("Dataset introuvable avec l'ID: " + datasetId));

        // Récupérer tous les couples de texte du dataset
        List<coupleTexte> coupleTextes = coupleTexteRepository.findByDataSet(dataset);

        if (coupleTextes.isEmpty()) {
            throw new IllegalArgumentException("Le dataset ne contient aucun couple de texte");
        }

        // Mélanger les couples de texte pour une distribution aléatoire
        Collections.shuffle(coupleTextes);

        // Récupérer les annotateurs sélectionnés
        List<Annotateur> annotateurs = annotateurRepository.findAllById(annotateurIds);

        if (annotateurs.isEmpty()) {
            throw new IllegalArgumentException("Aucun annotateur trouvé avec les IDs fournis");
        }

        // Nombre total de couples de texte
        int totalCouples = coupleTextes.size();
        // Nombre d'annotateurs
        int nombreAnnotateurs = annotateurs.size();

        // Créer une liste pour stocker les tâches créées
        List<Tache> tachesCreees = new ArrayList<>();

        // Distribuer aléatoirement les couples aux annotateurs
        if (totalCouples <= nombreAnnotateurs) {
            // S'il y a moins de couples que d'annotateurs, certains annotateurs n'auront pas de tâche
            for (int i = 0; i < totalCouples; i++) {
                Tache tache = new Tache();
                tache.setData(dataset);
                tache.setAnnotateur(annotateurs.get(i));

                List<coupleTexte> couplesForTache = new ArrayList<>();
                coupleTexte couple = coupleTextes.get(i);
                couplesForTache.add(couple);
                tache.setCoupleTexte(couplesForTache);
                tache.setDateLimite(dateLimite);

                // Sauvegarder la tâche
                Tache savedTache = tacheRepository.save(tache);

                // Mettre à jour la relation bidirectionnelle
                couple.setTache(savedTache);
                coupleTexteRepository.save(couple);

                tachesCreees.add(savedTache);
            }
        } else {
            // S'il y a plus de couples que d'annotateurs, répartir aléatoirement

            // Créer une liste d'indices pour les couples
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < totalCouples; i++) {
                indices.add(i);
            }

            // Mélanger les indices
            Collections.shuffle(indices);

            // Calculer le nombre minimum de couples par annotateur
            int minCouplesPerAnnotateur = totalCouples / nombreAnnotateurs;
            // Calculer combien d'annotateurs auront un couple supplémentaire
            int extraCouples = totalCouples % nombreAnnotateurs;

            // Index pour suivre la position dans la liste des couples
            int currentIndex = 0;

            // Distribuer les couples aux annotateurs
            for (int i = 0; i < nombreAnnotateurs; i++) {
                // Nombre de couples pour cet annotateur
                int couplesForThisAnnotateur = minCouplesPerAnnotateur + (i < extraCouples ? 1 : 0);

                if (couplesForThisAnnotateur > 0) {
                    // Créer une nouvelle tâche
                    Tache tache = new Tache();
                    tache.setData(dataset);
                    tache.setAnnotateur(annotateurs.get(i));

                    // Ajouter les couples de texte à la tâche
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

                    // Sauvegarder la tâche
                    Tache savedTache = tacheRepository.save(tache);

                    // Mettre à jour la relation bidirectionnelle
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
}
