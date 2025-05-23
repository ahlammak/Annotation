package com.example.annotationprojet.web.admin;

import com.example.annotationprojet.Service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Affiche la page des statistiques
     */
    @GetMapping("/admin/statistics")
    public String showStatisticsPage(Model model) {
        try {
            // Récupérer toutes les statistiques
            Map<String, Object> statistics = statisticsService.getAllStatistics();
            model.addAttribute("statistics", statistics);
            
            // Récupérer les statistiques détaillées par dataset
            Map<String, Object> detailedStats = statisticsService.getDetailedDatasetStatistics();
            model.addAttribute("detailedStats", detailedStats);
            
            return "admin/statistics-standalone";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Erreur lors du chargement des statistiques: " + e.getMessage());
            return "error/custom-error";
        }
    }

    /**
     * API endpoint pour récupérer les statistiques en JSON (pour les mises à jour en temps réel)
     */
    @GetMapping("/admin/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatisticsApi() {
        try {
            Map<String, Object> statistics = statisticsService.getAllStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API endpoint pour récupérer les statistiques détaillées par dataset
     */
    @GetMapping("/admin/api/statistics/detailed")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDetailedStatisticsApi() {
        try {
            Map<String, Object> detailedStats = statisticsService.getDetailedDatasetStatistics();
            return ResponseEntity.ok(detailedStats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
