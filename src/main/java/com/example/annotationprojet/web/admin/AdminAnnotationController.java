package com.example.annotationprojet.web.admin;

import com.example.annotationprojet.Service.AnnotationService;
import com.example.annotationprojet.Service.DataSetService;
import com.example.annotationprojet.Service.ProgressService;
import com.example.annotationprojet.entities.Classes;
import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.entities.coupleTexte;
import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.Tache;
import com.example.annotationprojet.repositories.ClassesRepository;
import com.example.annotationprojet.repositories.coupleTexteRepository;
import com.example.annotationprojet.repositories.AnnotateurRepository;
import com.example.annotationprojet.repositories.TacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Controller
@RequestMapping("/admin")
public class AdminAnnotationController {

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private coupleTexteRepository coupleTexteRepository;

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private ProgressService progressService;

    /**
     * Affiche la page des annotations d'un dataset
     * @param datasetId L'ID du dataset
     * @param model Le modèle pour la vue
     * @return La vue à afficher
     */
    @GetMapping("/viewAnnotations")
    public String viewAnnotations(@RequestParam("datasetId") Integer datasetId, Model model, HttpServletRequest request) {
        try {
            // Récupérer le dataset
            DataSet dataset = dataSetService.getDataSetById(datasetId);
            if (dataset == null) {
                model.addAttribute("errorMessage", "Dataset non trouvé");
                return "error/custom-error";
            }

            // Récupérer tous les couples de texte du dataset
            List<coupleTexte> allCouples = coupleTexteRepository.findByDataSet(dataset);

            // Récupérer les couples annotés
            List<coupleTexte> annotatedCouples = annotationService.getAnnotatedCouplesByDataset(datasetId);

            // Récupérer les classes disponibles
            List<Classes> classes = classesRepository.findByDataSet(dataset);

            // Récupérer tous les annotateurs actifs
            List<Annotateur> annotateurs = annotateurRepository.findByActiveTrue();

            // Récupérer les annotateurs affectés à ce dataset
            Set<Integer> annotateurIdsDejaAffectes = new HashSet<>();
            List<Tache> taches = tacheRepository.findByData(dataset);

            if (taches != null) {
                for (Tache tache : taches) {
                    if (tache != null && tache.getAnnotateur() != null) {
                        annotateurIdsDejaAffectes.add(tache.getAnnotateur().getID());
                    }
                }
            }

            // Calculer les statistiques d'avancement
            try {
                Map<String, Object> statistics = progressService.calculateDataSetStatistics(dataset);
                model.addAttribute("statistics", statistics);

                // Calculer l'avancement par annotateur
                Map<Integer, Integer> progressByAnnotateur = progressService.calculateAnnotateurProgress(dataset);
                model.addAttribute("progressByAnnotateur", progressByAnnotateur);

                System.out.println("Statistiques d'avancement calculées : " + statistics);
                System.out.println("Nombre de couples annotés récupérés : " + (annotatedCouples != null ? annotatedCouples.size() : "null"));
            } catch (Exception e) {
                System.err.println("Erreur lors du calcul de l'avancement : " + e.getMessage());
                e.printStackTrace();
            }

            // Ajouter les données au modèle
            model.addAttribute("dataset", dataset);
            model.addAttribute("allCouples", allCouples);
            model.addAttribute("annotatedCouples", annotatedCouples);
            model.addAttribute("classes", classes);
            model.addAttribute("annotateurs", annotateurs);
            model.addAttribute("annotateurIdsDejaAffectes", annotateurIdsDejaAffectes);
            model.addAttribute("httpServletRequest", request);

            return "admin/Dataset/viewAnnotations";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Une erreur s'est produite: " + e.getMessage());
            return "error/custom-error";
        }
    }

    /**
     * Modifie une annotation
     * @param annotationId L'ID de l'annotation
     * @param classeChoisie La nouvelle classe choisie
     * @return Le résultat de l'opération
     */
    @PostMapping("/updateAnnotation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAnnotation(
            @RequestParam("annotationId") Integer annotationId,
            @RequestParam("classeChoisie") String classeChoisie) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean success = annotationService.updateAnnotation(annotationId, classeChoisie);

            if (success) {
                response.put("success", true);
                response.put("message", "Annotation modifiée avec succès");
            } else {
                response.put("success", false);
                response.put("message", "Erreur lors de la modification de l'annotation");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}
