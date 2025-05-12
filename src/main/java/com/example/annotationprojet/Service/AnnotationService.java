package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.*;
import com.example.annotationprojet.repositories.AnnotationRepository;
import com.example.annotationprojet.repositories.TacheRepository;
import com.example.annotationprojet.repositories.coupleTexteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AnnotationService {

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private coupleTexteRepository coupleTexteRepository;

    @Autowired
    private AnnotationRepository annotationRepository;

    /**
     * Récupère une tâche par son ID
     * @param tacheId L'ID de la tâche
     * @return La tâche ou null si elle n'existe pas
     */
    public Tache getTacheById(Integer tacheId) {
        return tacheRepository.findById(tacheId).orElse(null);
    }

    /**
     * Récupère les couples de textes d'une tâche
     * @param tache La tâche
     * @return La liste des couples de textes
     */
    public List<coupleTexte> getCoupleTextesByTache(Tache tache) {
        return coupleTexteRepository.findByTache(tache);
    }

    /**
     * Récupère un couple de textes par son ID
     * @param coupleId L'ID du couple de textes
     * @return Le couple de textes ou null s'il n'existe pas
     */
    public coupleTexte getCoupleTexteById(Integer coupleId) {
        return coupleTexteRepository.findById(coupleId).orElse(null);
    }

    /**
     * Récupère les classes disponibles pour un dataset
     * @param dataSet Le dataset
     * @return La liste des classes
     */
    public List<Classes> getClassesByDataSet(DataSet dataSet) {
        return dataSet.getClasses();
    }

    /**
     * Sauvegarde une annotation pour un couple de textes
     * @param coupleId L'ID du couple de textes
     * @param classeChoisie La classe choisie
     * @param annotateur L'annotateur
     * @return true si l'annotation a été sauvegardée, false sinon
     */
    @Transactional
    public boolean saveAnnotation(Integer coupleId, String classeChoisie, Annotateur annotateur) {
        try {
            // Récupérer le couple de textes
            Optional<coupleTexte> optionalCouple = coupleTexteRepository.findById(coupleId);
            if (optionalCouple.isEmpty()) {
                return false;
            }

            coupleTexte couple = optionalCouple.get();

            // Vérifier si une annotation existe déjà
            Annotation annotation = couple.getAnnotation();

            if (annotation == null) {
                // Créer une nouvelle annotation
                annotation = new Annotation();
                annotation.setID(coupleId); // Utiliser le même ID que le couple de textes
                annotation.setTypeChoisie(classeChoisie);
                annotation.setAnnotateur(annotateur);

                // Sauvegarder l'annotation
                annotation = annotationRepository.save(annotation);

                // Mettre à jour la référence dans le couple de textes
                couple.setAnnotation(annotation);
                coupleTexteRepository.save(couple);
            } else {
                // Mettre à jour la classe choisie
                annotation.setTypeChoisie(classeChoisie);

                // Sauvegarder l'annotation
                annotationRepository.save(annotation);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
