package com.example.annotationprojet.web.admin;

import com.example.annotationprojet.Service.AnnotationService;
import com.example.annotationprojet.Service.DataSetService;
import com.example.annotationprojet.entities.Classes;
import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.entities.coupleTexte;
import com.example.annotationprojet.repositories.ClassesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminAnnotationController {

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private ClassesRepository classesRepository;

    /**
     * Affiche la page des annotations d'un dataset
     * @param datasetId L'ID du dataset
     * @param model Le modèle pour la vue
     * @return La vue à afficher
     */
    @GetMapping("/viewAnnotations")
    public String viewAnnotations(@RequestParam("datasetId") Integer datasetId, Model model) {
        try {
            // Récupérer le dataset
            DataSet dataset = dataSetService.getDataSetById(datasetId);
            if (dataset == null) {
                model.addAttribute("errorMessage", "Dataset non trouvé");
                return "error/custom-error";
            }

            // Récupérer les couples annotés
            List<coupleTexte> annotatedCouples = annotationService.getAnnotatedCouplesByDataset(datasetId);

            // Récupérer les classes disponibles
            List<Classes> classes = classesRepository.findByDataSet(dataset);

            // Ajouter les données au modèle
            model.addAttribute("dataset", dataset);
            model.addAttribute("annotatedCouples", annotatedCouples);
            model.addAttribute("classes", classes);

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
