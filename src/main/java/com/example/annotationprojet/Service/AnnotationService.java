package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.*;
import com.example.annotationprojet.repositories.AnnotationRepository;
import com.example.annotationprojet.repositories.DataSetRepository;
import com.example.annotationprojet.repositories.TacheRepository;
import com.example.annotationprojet.repositories.coupleTexteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnnotationService {

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private coupleTexteRepository coupleTexteRepository;

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    public Tache getTacheById(Integer tacheId) {
        return tacheRepository.findById(tacheId).orElse(null);
    }
    public List<coupleTexte> getCoupleTextesByTache(Tache tache) {
        return coupleTexteRepository.findByTache(tache);
    }

    public coupleTexte getCoupleTexteById(Integer coupleId) {
        return coupleTexteRepository.findById(coupleId).orElse(null);
    }

    public List<Classes> getClassesByDataSet(DataSet dataSet) {
        return dataSet.getClasses();
    }


    /**
     * Récupère tous les couples de textes annotés d'un dataset
     * @param datasetId L'ID du dataset
     * @return Liste des couples de textes annotés
     */
    public List<coupleTexte> getAnnotatedCouplesByDataset(Integer datasetId) {
        DataSet dataSet = dataSetRepository.findById(datasetId).orElse(null);
        if (dataSet == null) {
            return new ArrayList<>();
        }

        List<coupleTexte> allCouples = coupleTexteRepository.findByDataSet(dataSet);
        return allCouples.stream()
                .filter(couple -> couple.getAnnotation() != null)
                .collect(Collectors.toList());
    }

    /**
     * Modifie une annotation existante
     * @param annotationId L'ID de l'annotation (même que coupleId)
     * @param classeChoisie La nouvelle classe choisie
     * @return true si la modification a réussi, false sinon
     */
    @Transactional
    public boolean updateAnnotation(Integer annotationId, String classeChoisie) {
        try {
            Optional<Annotation> optionalAnnotation = annotationRepository.findById(annotationId);
            if (optionalAnnotation.isEmpty()) {
                return false;
            }

            Annotation annotation = optionalAnnotation.get();
            annotation.setTypeChoisie(classeChoisie);
            annotationRepository.save(annotation);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean saveAnnotation(Integer coupleId, String classeChoisie, Annotateur annotateur) {
        try {
            Optional<coupleTexte> optionalCouple = coupleTexteRepository.findById(coupleId);
            if (optionalCouple.isEmpty()) {
                return false;
            }

            coupleTexte couple = optionalCouple.get();


            Annotation annotation = couple.getAnnotation();

            if (annotation == null) {

                annotation = new Annotation();
                annotation.setID(coupleId);
                annotation.setTypeChoisie(classeChoisie);
                annotation.setAnnotateur(annotateur);

                annotation = annotationRepository.save(annotation);

                couple.setAnnotation(annotation);
                coupleTexteRepository.save(couple);
            } else {

                annotation.setTypeChoisie(classeChoisie);

                annotationRepository.save(annotation);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
