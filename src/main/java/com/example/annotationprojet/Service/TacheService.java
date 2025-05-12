package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.Tache;
import com.example.annotationprojet.repositories.TacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TacheService {

    @Autowired
    private TacheRepository tacheRepository;
    public List<Tache> getTachesByAnnotateur(Annotateur annotateur) {
        return tacheRepository.findByAnnotateur(annotateur);
    }
}
