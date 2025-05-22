package com.example.annotationprojet.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur pour la page de gestion des tâches
 */
@Controller
public class TasksViewController {

    /**
     * Affiche la page de gestion des tâches
     * @return Le nom de la vue
     */
    @GetMapping("/admin/tasks")
    public String showTasksPage() {
        return "admin/tasks";
    }
}
