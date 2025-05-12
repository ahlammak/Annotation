package com.example.annotationprojet.web.user;

import com.example.annotationprojet.Service.TacheService;
import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.CustomUserDetails;
import com.example.annotationprojet.entities.Tache;
import com.example.annotationprojet.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserTaskController {

    @Autowired
    private TacheService tacheService;

    /**
     * Affiche la liste des tâches assignées à l'utilisateur connecté
     * @param model Le modèle pour la vue
     * @return La vue à afficher
     */
    @GetMapping("/user/tasks")
    public String listUserTasks(Model model) {
        // Récupérer l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        
        // Vérifier que l'utilisateur est bien un annotateur
        if (!(user instanceof Annotateur)) {
            return "redirect:/user/user?error=not_annotator";
        }
        
        Annotateur annotateur = (Annotateur) user;
        
        // Récupérer les tâches de l'annotateur
        List<Tache> taches = tacheService.getTachesByAnnotateur(annotateur);
        
        // Ajouter les tâches au modèle
        model.addAttribute("taches", taches);
        model.addAttribute("annotateur", annotateur);
        
        return "user/listTasks";
    }
}
