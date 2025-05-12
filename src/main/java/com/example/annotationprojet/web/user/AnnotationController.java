package com.example.annotationprojet.web.user;

import com.example.annotationprojet.Service.AnnotationService;
import com.example.annotationprojet.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AnnotationController {

    @Autowired
    private AnnotationService annotationService;

    /**
     * Affiche l'interface d'annotation pour une tâche
     * @param tacheId L'ID de la tâche
     * @param model Le modèle pour la vue
     * @return La vue à afficher
     */
    @GetMapping("/user/viewTask")
    public String viewTask(@RequestParam("id") Integer tacheId, Model model) {
        try {
            // Récupérer la tâche
            Tache tache = annotationService.getTacheById(tacheId);
            if (tache == null) {
                return "redirect:/user/tasks?error=task_not_found";
            }
            
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            // Vérifier que l'utilisateur est bien un annotateur
            if (!(user instanceof Annotateur)) {
                return "redirect:/user/tasks?error=not_annotator";
            }
            
            Annotateur annotateur = (Annotateur) user;
            
            // Vérifier que la tâche appartient bien à l'annotateur
            if (!tache.getAnnotateur().getID().equals(annotateur.getID())) {
                return "redirect:/user/tasks?error=not_your_task";
            }
            
            // Récupérer les couples de textes
            List<coupleTexte> coupleTextes = annotationService.getCoupleTextesByTache(tache);
            
            // Récupérer les classes disponibles
            List<Classes> classes = annotationService.getClassesByDataSet(tache.getData());
            
            // Ajouter les données au modèle
            model.addAttribute("tache", tache);
            model.addAttribute("coupleTextes", coupleTextes);
            model.addAttribute("classes", classes);
            model.addAttribute("annotateur", annotateur);
            model.addAttribute("totalCouples", coupleTextes.size());
            model.addAttribute("progress", tache.getProgressPercentage());
            
            return "user/viewTask";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Une erreur s'est produite lors du chargement de la tâche: " + e.getMessage());
            return "error/custom-error";
        }
    }
    
    /**
     * Sauvegarde une annotation pour un couple de textes
     * @param coupleId L'ID du couple de textes
     * @param classeChoisie La classe choisie
     * @return Le résultat de l'opération
     */
    @PostMapping("/user/saveAnnotation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveAnnotation(
            @RequestParam("coupleId") Integer coupleId,
            @RequestParam("classeChoisie") String classeChoisie) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            // Vérifier que l'utilisateur est bien un annotateur
            if (!(user instanceof Annotateur)) {
                response.put("success", false);
                response.put("message", "Vous n'êtes pas un annotateur");
                return ResponseEntity.badRequest().body(response);
            }
            
            Annotateur annotateur = (Annotateur) user;
            
            // Sauvegarder l'annotation
            boolean success = annotationService.saveAnnotation(coupleId, classeChoisie, annotateur);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Annotation sauvegardée avec succès");
                
                // Récupérer le couple de textes pour obtenir la tâche
                coupleTexte couple = annotationService.getCoupleTexteById(coupleId);
                if (couple != null && couple.getTache() != null) {
                    Tache tache = couple.getTache();
                    response.put("progress", tache.getProgressPercentage());
                }
            } else {
                response.put("success", false);
                response.put("message", "Erreur lors de la sauvegarde de l'annotation");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Récupère les détails d'un couple de textes
     * @param coupleId L'ID du couple de textes
     * @return Les détails du couple de textes
     */
    @GetMapping("/user/getCoupleDetails")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCoupleDetails(@RequestParam("coupleId") Integer coupleId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Récupérer le couple de textes
            coupleTexte couple = annotationService.getCoupleTexteById(coupleId);
            if (couple == null) {
                response.put("success", false);
                response.put("message", "Couple de textes non trouvé");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("id", couple.getID());
            response.put("texte1", couple.getTexte1());
            response.put("texte2", couple.getTexte2());
            
            // Ajouter l'annotation si elle existe
            if (couple.getAnnotation() != null) {
                response.put("annotation", couple.getAnnotation().getTypeChoisie());
            } else {
                response.put("annotation", null);
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
