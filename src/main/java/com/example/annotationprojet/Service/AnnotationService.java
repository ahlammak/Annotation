package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.*;

import java.util.List;

public interface AnnotationService {
    boolean saveAnnotation(Integer coupleId, String classeChoisie, Annotateur annotateur);

    boolean updateAnnotation(Integer annotationId, String classeChoisie);
    public coupleTexte getCoupleTexteById(Integer coupleId);
    public Tache getTacheById(Integer tacheId);
    public List<coupleTexte> getCoupleTextesByTache(Tache tache);
    public List<Classes> getClassesByDataSet(DataSet dataSet);
    public List<coupleTexte> getAnnotatedCouplesByDataset(Integer datasetId);

}
