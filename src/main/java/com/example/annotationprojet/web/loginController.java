package com.example.annotationprojet.web;

import com.example.annotationprojet.Service.StatisticsService;
import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.CustomUserDetails;
import com.example.annotationprojet.entities.User;
import com.example.annotationprojet.repositories.AnnotateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class loginController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/")
    public String home() {
        return "auth/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/admin/admin")
    public String showAdminHome(Model model) {
        try {
            // Récupérer toutes les statistiques comme dans StatisticsController
            Map<String, Object> statistics = statisticsService.getAllStatistics();
            model.addAttribute("statistics", statistics);

            return "admin/admin-standalone";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Erreur lors du chargement des statistiques: " + e.getMessage());
            return "admin/admin-standalone";
        }
    }
    @GetMapping("/user/user")
    @Transactional(readOnly = true)
    public String showUserHome(Model model) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();

            Annotateur annotateur = null;

            // Gérer différents types de principal
            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                User user = userDetails.getUser();
                if (user instanceof Annotateur) {
                    annotateur = (Annotateur) user;
                }
            } else if (principal instanceof UserDetails && !(principal instanceof CustomUserDetails)) {
                // Cas où principal est un UserDetails mais pas un CustomUserDetails
                UserDetails userDetails = (UserDetails) principal;
                String username = userDetails.getUsername();
                annotateur = annotateurRepository.findByLogin(username);
            }

            // Calculer les statistiques spécifiques à l'annotateur
            Map<String, Object> userStats = statisticsService.getAnnotateurSpecificStatistics(annotateur);

            // Ajouter les statistiques au modèle
            model.addAttribute("totalTaches", userStats.get("totalTaches"));
            model.addAttribute("tachesTerminees", userStats.get("tachesTerminees"));
            model.addAttribute("tachesEnCours", userStats.get("tachesEnCours"));
            model.addAttribute("totalAnnotations", userStats.get("totalAnnotations"));
            model.addAttribute("annotateur", annotateur);

            System.out.println("Statistiques utilisateur calculées: " + userStats);

            return "user/user-standalone";
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des statistiques utilisateur: " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, retourner des valeurs par défaut
            model.addAttribute("totalTaches", 0);
            model.addAttribute("tachesTerminees", 0);
            model.addAttribute("tachesEnCours", 0);
            model.addAttribute("totalAnnotations", 0);

            return "user/user-standalone";
        }
    }
}