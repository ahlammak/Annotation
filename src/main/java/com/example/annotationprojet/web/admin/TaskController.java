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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @Autowired
    private AffectationAnnotateurService affectationAnnotateurService;

    @Autowired
    private TacheRepository tacheRepository;



    @GetMapping("/admin/assignTasks")
    public String showAssignTasksPage(Model model) {
        try {
            logger.info("Début de la méthode showAssignTasksPage");

            List<DataSet> allDatasets = dataSetService.getAllDataSets();
            logger.info("Nombre total de datasets: {}", allDatasets.size());

            List<DataSet> availableDatasets = new ArrayList<>();
            List<Integer> datasetsDejaAffectes = new ArrayList<>();


            for (DataSet dataset : allDatasets) {
                try {
                    List<Tache> taches = tacheRepository.findByData(dataset);
                    if (taches != null && !taches.isEmpty()) {
                        // Ce dataset a déjà des tâches, l'ajouter à la liste des datasets déjà affectés
                        datasetsDejaAffectes.add(dataset.getID());
                        logger.info("Dataset {} (ID: {}) a déjà des tâches affectées", dataset.getNom(), dataset.getID());
                    } else {
                        // Ce dataset n'a pas encore de tâches, l'ajouter à la liste des datasets disponibles
                        availableDatasets.add(dataset);
                        logger.info("Dataset {} (ID: {}) est disponible pour affectation", dataset.getNom(), dataset.getID());
                    }
                } catch (Exception e) {
                    logger.error("Erreur lors de la vérification des tâches pour le dataset {}: {}", dataset.getNom(), e.getMessage(), e);
                    // En cas d'erreur, considérer le dataset comme disponible
                    availableDatasets.add(dataset);
                }
            }

            // Récupérer tous les annotateurs actifs
            List<Annotateur> annotateurs = annotateurRepository.findByActiveTrue();
            logger.info("Nombre d'annotateurs actifs: {}", annotateurs.size());

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

            logger.info("Fin de la méthode showAssignTasksPage");
            return "admin/Dataset/assignTasks-standalone";
        } catch (Exception e) {
            logger.error("Erreur dans showAssignTasksPage: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Une erreur s'est produite lors du chargement de la page: " + e.getMessage());
            // Ajouter des attributs vides pour éviter les erreurs dans la vue
            model.addAttribute("datasets", new ArrayList<DataSet>());
            model.addAttribute("datasetsDejaAffectes", new ArrayList<DataSet>());
            model.addAttribute("datasetsDejaAffectesIds", new ArrayList<Integer>());
            model.addAttribute("allDatasets", new ArrayList<DataSet>());
            model.addAttribute("annotateurs", new ArrayList<Annotateur>());
            return "admin/Dataset/assignTasks-standalone";
        }
    }


    @PostMapping("/admin/processTaskAssignment")
    public String processTaskAssignment(
            @RequestParam(value = "datasetId", required = false) Integer datasetId,
            @RequestParam(value = "annotateurIds", required = false) List<Integer> annotateurIds,
            @RequestParam(value = "dateLimite", required = false) String dateLimite,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Début de la méthode processTaskAssignment");
            logger.info("datasetId = {}", datasetId);
            logger.info("annotateurIds = {}", (annotateurIds != null ? annotateurIds : "null"));
            logger.info("dateLimite = {}", dateLimite);

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

                // S'assurer que l'heure, les minutes et les secondes sont à zéro
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateLimit);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                dateLimit = calendar.getTime();
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

            logger.info("Fin de la méthode processTaskAssignment");
            return "redirect:/admin/assignTasks";

        } catch (Exception e) {
            // En cas d'erreur, ajouter un message d'erreur
            logger.error("Erreur dans processTaskAssignment: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("message", "Erreur lors de l'affectation des tâches : " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            return "redirect:/admin/assignTasks";
        }
    }


    @PostMapping("/admin/affecterTaches")
    public String affecterTaches(
            @RequestParam("datasetId") Integer datasetId,
            @RequestParam(value = "annotateurIds", required = false) List<Integer> annotateurIds,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Début de la méthode affecterTaches");
            logger.info("datasetId = {}", datasetId);
            logger.info("annotateurIds = {}", (annotateurIds != null ? annotateurIds : "null"));

            // Vérifier que des annotateurs ont été sélectionnés
            if (annotateurIds == null || annotateurIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "Veuillez sélectionner au moins un annotateur.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/viewDataset?id=" + datasetId;
            }

            // Créer une date limite par défaut (30 jours à partir d'aujourd'hui)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 30);
            // S'assurer que l'heure, les minutes et les secondes sont à zéro
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date dateLimite = calendar.getTime();

            // Affecter les tâches avec la date limite
            int nombreTaches = affectationAnnotateurService.affecterTachesAleatoires(datasetId, annotateurIds, dateLimite);

            // Formater la date pour l'affichage
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dateLimiteStr = dateFormat.format(dateLimite);

            // Ajouter un message de succès
            redirectAttributes.addFlashAttribute("message", nombreTaches + " tâches ont été créées avec succès pour la date limite du " + dateLimiteStr + ".");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");

            logger.info("Fin de la méthode affecterTaches");
        } catch (Exception e) {
            // En cas d'erreur, ajouter un message d'erreur
            logger.error("Erreur dans affecterTaches: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("message", "Erreur lors de l'affectation des tâches : " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        // Rediriger vers la page de détails du dataset
        return "redirect:/admin/viewDataset?id=" + datasetId;
    }
}
