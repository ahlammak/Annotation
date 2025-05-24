package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.DataSet;

import java.util.Date;
import java.util.List;

public interface AffectationAnnotateurService {
    int affecterTachesAleatoires(Integer datasetId, List<Integer> annotateurIds);

    int affecterTachesAleatoires(Integer datasetId, List<Integer> annotateurIds, Date dateLimite);

    int reassignTasks(DataSet dataset, Annotateur annotateurToRemove);

    int reassignTasksForAllDatasets(Annotateur annotateurToRemove);
}
