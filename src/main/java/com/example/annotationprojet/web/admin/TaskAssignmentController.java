package com.example.annotationprojet.web.admin;

import com.example.annotationprojet.Service.AffectationAnnotateurService;
import com.example.annotationprojet.Service.DataSetService;
import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.entities.Tache;
import com.example.annotationprojet.repositories.AnnotateurRepository;
import com.example.annotationprojet.repositories.TacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class TaskAssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(TaskAssignmentController.class);

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @Autowired
    private AffectationAnnotateurService affectationAnnotateurService;

    @Autowired
    private TacheRepository tacheRepository;

    /**
     * Affiche la page d'affectation des tâches
     * @param model Le modèle
     * @return La vue
     */
    @GetMapping("/admin/assignTasks")
    public String showAssignTasksPage(Model model) {
        try {
            System.out.println("Début de la méthode showAssignTasksPage");

            // Récupérer tous les datasets
            List<DataSet> allDatasets = dataSetService.getAllDataSets();
            System.out.println("Nombre total de datasets: " + allDatasets.size());

            // Listes pour stocker les datasets disponibles et ceux déjà affectés
            List<DataSet> availableDatasets = new ArrayList<>();
            List<Integer> datasetsDejaAffectes = new ArrayList<>();

            // Vérifier quels datasets ont déjà des tâches affectées
            for (DataSet dataset : allDatasets) {
                try {
                    List<Tache> taches = tacheRepository.findByData(dataset);
                    if (taches != null && !taches.isEmpty()) {
                        // Ce dataset a déjà des tâches, l'ajouter à la liste des datasets déjà affectés
                        datasetsDejaAffectes.add(dataset.getID());
                        System.out.println("Dataset " + dataset.getNom() + " (ID: " + dataset.getID() + ") a déjà des tâches affectées");
                    } else {
                        // Ce dataset n'a pas encore de tâches, l'ajouter à la liste des datasets disponibles
                        availableDatasets.add(dataset);
                        System.out.println("Dataset " + dataset.getNom() + " (ID: " + dataset.getID() + ") est disponible pour affectation");
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la vérification des tâches pour le dataset " + dataset.getNom() + ": " + e.getMessage());
                    e.printStackTrace();
                    // En cas d'erreur, considérer le dataset comme disponible
                    availableDatasets.add(dataset);
                }
            }

            // Récupérer tous les annotateurs actifs
            List<Annotateur> annotateurs = annotateurRepository.findByActiveTrue();
            System.out.println("Nombre d'annotateurs actifs: " + annotateurs.size());

            // Ajouter les listes au modèle
            model.addAttribute("datasets", availableDatasets);

            // Créer une liste de datasets déjà affectés (objets complets, pas juste les IDs)
            List<DataSet> datasetsDejaAffectesObjets = new ArrayList<>();
            for (Integer id : datasetsDejaAffectes) {
                for (DataSet ds : allDatasets) {
                    if (ds.getID().equals(id)) {
                        datasetsDejaAffectesObjets.add(ds);
                        break;
                    }
                }
            }

            model.addAttribute("datasetsDejaAffectes", datasetsDejaAffectesObjets);
            model.addAttribute("datasetsDejaAffectesIds", datasetsDejaAffectes);
            model.addAttribute("allDatasets", allDatasets);
            model.addAttribute("annotateurs", annotateurs);

            System.out.println("Fin de la méthode showAssignTasksPage");
            return "admin/Dataset/AssignTasks";
        } catch (Exception e) {
            System.err.println("Erreur dans showAssignTasksPage: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Une erreur s'est produite lors du chargement de la page: " + e.getMessage());
            // Ajouter des attributs vides pour éviter les erreurs dans la vue
            model.addAttribute("datasets", new ArrayList<DataSet>());
            model.addAttribute("datasetsDejaAffectes", new ArrayList<Integer>());
            model.addAttribute("allDatasets", new ArrayList<DataSet>());
            model.addAttribute("annotateurs", new ArrayList<Annotateur>());
            return "admin/Dataset/AssignTasks";
        }
    }

    /**
     * Traite l'affectation des tâches
     * @param datasetId L'ID du dataset
     * @param annotateurIds Les IDs des annotateurs
     * @param dateLimite La date limite pour les tâches
     * @param redirectAttributes Les attributs de redirection
     * @return La redirection
     */
    @PostMapping("/admin/processTaskAssignment")
    public String processTaskAssignment(
            @RequestParam(value = "datasetId", required = false) Integer datasetId,
            @RequestParam(value = "annotateurIds", required = false) List<Integer> annotateurIds,
            @RequestParam(value = "dateLimite", required = false) String dateLimite,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("Début de la méthode processTaskAssignment");
            System.out.println("datasetId = " + datasetId);
            System.out.println("annotateurIds = " + (annotateurIds != null ? annotateurIds : "null"));
            System.out.println("dateLimite = " + dateLimite);

            // Vérifier que le dataset a été sélectionné
            if (datasetId == null) {
                redirectAttributes.addFlashAttribute("message", "Veuillez sélectionner un dataset.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/assignTasks";
            }

            // Vérifier que des annotateurs ont été sélectionnés
            if (annotateurIds == null || annotateurIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "Veuillez sélectionner au moins un annotateur.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/assignTasks";
            }

            // Vérifier que la date limite a été spécifiée
            if (dateLimite == null || dateLimite.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "Veuillez spécifier une date limite.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/assignTasks";
            }

            // Convertir la date limite de String à Date
            Date dateLimit = null;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateLimit = dateFormat.parse(dateLimite);
            } catch (ParseException e) {
                redirectAttributes.addFlashAttribute("message", "Format de date invalide. Veuillez utiliser le format YYYY-MM-DD.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/assignTasks";
            }

            // Affecter les tâches avec la date limite
            int nombreTaches = affectationAnnotateurService.affecterTachesAleatoires(datasetId, annotateurIds, dateLimit);

            // Ajouter un message de succès
            redirectAttributes.addFlashAttribute("message", nombreTaches + " tâches ont été créées avec succès pour la date limite du " + dateLimite + ".");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");

            System.out.println("Fin de la méthode processTaskAssignment");
            return "redirect:/admin/assignTasks";

        } catch (Exception e) {
            // En cas d'erreur, ajouter un message d'erreur
            System.err.println("Erreur dans processTaskAssignment: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Erreur lors de l'affectation des tâches : " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            return "redirect:/admin/assignTasks";
        }
    }
}
