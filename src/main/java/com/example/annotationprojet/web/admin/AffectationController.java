package com.example.annotationprojet.web.admin;

import com.example.annotationprojet.Service.AffectationAnnotateurService;
import com.example.annotationprojet.Service.DataSetService;
import com.example.annotationprojet.entities.DataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AffectationController {

    @Autowired
    private AffectationAnnotateurService affectationAnnotateurService;

    @Autowired
    private DataSetService dataSetService;

    @PostMapping("/admin/affecterTaches")
    public String affecterTaches(
            @RequestParam("datasetId") Integer datasetId,
            @RequestParam(value = "annotateurIds", required = false) List<Integer> annotateurIds,
            RedirectAttributes redirectAttributes) {

        try {
            // Vérifier que des annotateurs ont été sélectionnés
            if (annotateurIds == null || annotateurIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "Veuillez sélectionner au moins un annotateur.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/viewDataset?id=" + datasetId;
            }

            // Affecter les tâches
            int nombreTaches = affectationAnnotateurService.affecterTachesAleatoires(datasetId, annotateurIds);

            // Ajouter un message de succès
            redirectAttributes.addFlashAttribute("message", nombreTaches + " tâches ont été créées avec succès.");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");

        } catch (Exception e) {
            // En cas d'erreur, ajouter un message d'erreur
            redirectAttributes.addFlashAttribute("message", "Erreur lors de l'affectation des tâches : " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        // Rediriger vers la page de détails du dataset
        return "redirect:/admin/viewDataset?id=" + datasetId;
    }
}
