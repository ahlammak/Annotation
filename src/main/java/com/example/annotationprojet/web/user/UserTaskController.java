package com.example.annotationprojet.web.user;

import com.example.annotationprojet.Service.TacheService;
import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.CustomUserDetails;
import com.example.annotationprojet.entities.Tache;
import com.example.annotationprojet.entities.User;
import com.example.annotationprojet.repositories.AnnotateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class UserTaskController {

    @Autowired
    private TacheService tacheService;

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @GetMapping("/user/tasks")
    public String listUserTasks(Model model) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();

            System.out.println("Principal class: " + (principal != null ? principal.getClass().getName() : "null"));

            // Gérer le cas où principal est une chaîne (cas de l'utilisateur anonyme)
            if (principal instanceof String) {
                System.err.println("Principal est une chaîne: " + principal);
                return "redirect:/login";
            }

            // Gérer le cas où principal est un UserDetails mais pas un CustomUserDetails
            if (principal instanceof UserDetails && !(principal instanceof CustomUserDetails)) {
                UserDetails userDetails = (UserDetails) principal;
                System.err.println("Principal est un UserDetails mais pas un CustomUserDetails: " + userDetails.getUsername());

                // Essayer de récupérer l'annotateur par son login
                String username = userDetails.getUsername();
                Annotateur annotateur = annotateurRepository.findByLogin(username);

                if (annotateur != null) {
                    System.out.println("Annotateur récupéré par login: " + annotateur.getNom() + " " + annotateur.getPrenom() + " (ID: " + annotateur.getID() + ")");

                    // Récupérer les tâches de l'annotateur
                    List<Tache> taches = tacheService.getTachesByAnnotateur(annotateur);
                    System.out.println("Nombre de tâches récupérées: " + (taches != null ? taches.size() : "null"));

                    // Ajouter les tâches au modèle
                    model.addAttribute("taches", taches);
                    model.addAttribute("annotateur", annotateur);

                    return "user/listTasks-standalone";
                } else {
                    System.err.println("Aucun annotateur trouvé avec le login: " + username);
                    return "redirect:/user/user?error=not_annotator";
                }
            }

            // Vérifier que le principal est bien un CustomUserDetails
            if (!(principal instanceof CustomUserDetails)) {
                System.err.println("Principal n'est pas une instance de CustomUserDetails: " + principal.getClass().getName());
                return "redirect:/user/user?error=authentication";
            }

            CustomUserDetails userDetails = (CustomUserDetails) principal;
            User user = userDetails.getUser();

            // Vérifier que l'utilisateur est bien un annotateur
            if (!(user instanceof Annotateur)) {
                System.err.println("L'utilisateur n'est pas un annotateur: " + user.getClass().getName());
                return "redirect:/user/user?error=not_annotator";
            }

            Annotateur annotateur = (Annotateur) user;
            System.out.println("Annotateur récupéré: " + annotateur.getNom() + " " + annotateur.getPrenom() + " (ID: " + annotateur.getID() + ")");

            // Récupérer les tâches de l'annotateur
            List<Tache> taches = tacheService.getTachesByAnnotateur(annotateur);
            System.out.println("Nombre de tâches récupérées: " + (taches != null ? taches.size() : "null"));

            // Si taches est null, initialiser une liste vide
            if (taches == null) {
                taches = new ArrayList<>();
            }

            // Ajouter les tâches au modèle
            model.addAttribute("taches", taches);
            model.addAttribute("annotateur", annotateur);

            return "user/listTasks";
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des tâches: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de la récupération des tâches: " + e.getMessage());
            return "error/custom-error";
        }
    }
}
