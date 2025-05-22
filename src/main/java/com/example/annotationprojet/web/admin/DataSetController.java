package com.example.annotationprojet.web.admin;
import com.example.annotationprojet.Service.AffectationAnnotateurService;
import com.example.annotationprojet.Service.AsyncDataSetService;
import com.example.annotationprojet.Service.DataSetService;
import com.example.annotationprojet.Service.ProgressService;
import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.DataSet;
import com.example.annotationprojet.entities.Tache;
import com.example.annotationprojet.entities.coupleTexte;
import com.example.annotationprojet.repositories.AnnotateurRepository;
import com.example.annotationprojet.repositories.TacheRepository;
import com.example.annotationprojet.repositories.coupleTexteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class DataSetController {

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private AsyncDataSetService asyncDataSetService;

    @Autowired
    private coupleTexteRepository coupleTexteRepository;

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private ProgressService progressService;

    @Autowired
    private AffectationAnnotateurService affectationAnnotateurService;

    @GetMapping("/admin/Dataset")
    public String datasetChoice() {
        return "admin/Dataset/DatasetChoice";
    }

    @GetMapping("/admin/addDataset")
    public String addDataset(Model model) {
        // Récupérer tous les datasets pour le formulaire d'ajout
        List<DataSet> datasets = dataSetService.getAllDataSets();
        model.addAttribute("datasets", datasets);
        return "admin/Dataset/Dataset";
    }

    @GetMapping("/admin/listDatasets")
    public String listDatasets(Model model) {
        // Récupérer tous les datasets pour l'affichage
        List<DataSet> datasets = dataSetService.getAllDataSets();
        model.addAttribute("datasets", datasets);
        return "admin/Dataset/listDatasets";
    }

    @GetMapping("/admin/viewDataset")
    public String viewDataset(@RequestParam("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Début de la méthode viewDataset avec id = " + id);

            DataSet dataSet = dataSetService.getDataSetById(id);
            if (dataSet == null) {
                System.out.println("Dataset introuvable avec id = " + id);
                redirectAttributes.addFlashAttribute("message", "Dataset introuvable.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/Dataset";
            }

            System.out.println("Dataset trouvé : " + dataSet.getNom());

            model.addAttribute("dataset", dataSet);

            List<coupleTexte> coupleTextes = new ArrayList<>();
            try {
                coupleTextes = coupleTexteRepository.findByDataSet(dataSet);
                System.out.println("Nombre de couples de textes récupérés : " + coupleTextes.size());
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération des couples de textes : " + e.getMessage());
                e.printStackTrace();
            }
            model.addAttribute("coupleTextes", coupleTextes);

            List<Annotateur> annotateurs = new ArrayList<>();
            try {
                annotateurs = annotateurRepository.findByActiveTrue();
                System.out.println("Nombre d'annotateurs actifs récupérés : " + annotateurs.size());
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération des annotateurs : " + e.getMessage());
                e.printStackTrace();
            }
            model.addAttribute("annotateurs", annotateurs);

            Set<Integer> annotateurIdsDejaAffectes = new HashSet<>();
            try {
                List<Tache> taches = tacheRepository.findByData(dataSet);
                System.out.println("Nombre de tâches récupérées : " + (taches != null ? taches.size() : "null"));

                if (taches != null) {
                    for (Tache tache : taches) {
                        if (tache != null && tache.getAnnotateur() != null) {
                            annotateurIdsDejaAffectes.add(tache.getAnnotateur().getID());
                        }
                    }
                }
                System.out.println("Nombre d'annotateurs déjà affectés : " + annotateurIdsDejaAffectes.size());
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération des tâches : " + e.getMessage());
                e.printStackTrace();
            }

            model.addAttribute("annotateurIdsDejaAffectes", annotateurIdsDejaAffectes);

            // Calculer l'avancement du dataset
            try {
                // Récupérer les statistiques d'avancement
                Map<String, Object> statistics = progressService.calculateDataSetStatistics(dataSet);
                model.addAttribute("statistics", statistics);

                // Récupérer l'avancement par annotateur
                Map<Integer, Integer> progressByAnnotateur = progressService.calculateAnnotateurProgress(dataSet);
                model.addAttribute("progressByAnnotateur", progressByAnnotateur);

                System.out.println("Statistiques d'avancement calculées : " + statistics);
            } catch (Exception e) {
                System.err.println("Erreur lors du calcul de l'avancement : " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("Fin de la méthode viewDataset, retour de la vue");

            return "admin/Dataset/viewDataset";
        } catch (Exception e) {
            System.err.println("Erreur générale dans viewDataset : " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Erreur lors de la récupération du dataset : " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
            return "redirect:/admin/Dataset";
        }
    }

    @GetMapping("/admin/deleteDataset")
    public String deleteDataset(@RequestParam("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            // Récupérer le dataset pour afficher son nom dans le message
            DataSet dataSet = dataSetService.getDataSetById(id);
            if (dataSet == null) {
                redirectAttributes.addFlashAttribute("message", "Dataset introuvable.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/listDatasets";
            }

            String datasetName = dataSet.getNom();

            // Vérifier si le dataset a des tâches associées
            if (dataSet.getTaches() != null && !dataSet.getTaches().isEmpty()) {
                System.out.println("Le dataset a " + dataSet.getTaches().size() + " tâches associées qui seront supprimées");
            }

            // Supprimer le dataset avec toutes ses dépendances
            dataSetService.deleteDataSet(id);

            redirectAttributes.addFlashAttribute("message", "Dataset '" + datasetName + "' supprimé avec succès.");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        } catch (RuntimeException e) {
            System.err.println("Erreur lors de la suppression du dataset : " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Erreur lors de la suppression du dataset : " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        } catch (Exception e) {
            System.err.println("Exception inattendue lors de la suppression du dataset : " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Erreur inattendue lors de la suppression du dataset : " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        return "redirect:/admin/listDatasets";
    }

    @GetMapping("/admin/removeAnnotateurFromDataset")
    public String removeAnnotateurFromDataset(
            @RequestParam("datasetId") Integer datasetId,
            @RequestParam("annotateurId") Integer annotateurId,
            RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Suppression de l'annotateur " + annotateurId + " du dataset " + datasetId);

            DataSet dataset = dataSetService.getDataSetById(datasetId);
            if (dataset == null) {
                redirectAttributes.addFlashAttribute("message", "Dataset introuvable.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/Dataset";
            }

            Annotateur annotateur = annotateurRepository.findById(annotateurId).orElse(null);
            if (annotateur == null) {
                redirectAttributes.addFlashAttribute("message", "Annotateur introuvable.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/viewDataset?id=" + datasetId;
            }


            // Vérifier s'il y a d'autres annotateurs pour ce dataset
            List<Tache> allTaches = tacheRepository.findByData(dataset);
            Set<Integer> otherAnnotateurIds = new HashSet<>();

            for (Tache tache : allTaches) {
                if (tache.getAnnotateur() != null && !tache.getAnnotateur().getID().equals(annotateurId)) {
                    otherAnnotateurIds.add(tache.getAnnotateur().getID());
                }
            }

            if (otherAnnotateurIds.isEmpty()) {
                // Aucun autre annotateur disponible, on supprime simplement les tâches
                List<Tache> taches = tacheRepository.findByDataAndAnnotateur(dataset, annotateur);
                System.out.println("Nombre de tâches à supprimer : " + (taches != null ? taches.size() : "null"));

                if (taches != null && !taches.isEmpty()) {
                    tacheRepository.deleteAll(taches);
                    System.out.println("Tâches supprimées avec succès");
                }

                redirectAttributes.addFlashAttribute("message", "Annotateur " + annotateur.getPrenom() + " " + annotateur.getNom() + " supprimé du dataset avec succès.");
            } else {
                // Réaffecter les tâches aux autres annotateurs
                int reassignedCount = affectationAnnotateurService.reassignTasks(dataset, annotateur);

                if (reassignedCount > 0) {
                    redirectAttributes.addFlashAttribute("message", "Annotateur " + annotateur.getPrenom() + " " + annotateur.getNom() +
                            " supprimé du dataset avec succès. " + reassignedCount + " tâches non annotées ont été réaffectées aux autres annotateurs.");
                } else {
                    redirectAttributes.addFlashAttribute("message", "Annotateur " + annotateur.getPrenom() + " " + annotateur.getNom() +
                            " supprimé du dataset avec succès. Aucune tâche non annotée à réaffecter.");
                }
            }

            redirectAttributes.addFlashAttribute("alertClass", "alert-success");

        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression de l'annotateur du dataset : " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Erreur lors de la suppression de l'annotateur du dataset : " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        return "redirect:/admin/viewDataset?id=" + datasetId;
    }

    @PostMapping("/admin/importDataset")
    public String importDataset(@RequestParam("file") MultipartFile file,
                               @RequestParam("nom") String nom,
                               @RequestParam("description") String description,
                               @RequestParam(value = "classes", required = false) String classes,
                               RedirectAttributes redirectAttributes) {
        try {
            // Vérifier que le fichier n'est pas vide
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Le fichier est vide");
            }

            // Créer le dataset et sauvegarder le fichier (sans traiter les données)
            DataSet importedDataSet = dataSetService.importDataSet(file, nom, description, classes);

            // Lancer le traitement asynchrone des données
            asyncDataSetService.processDataSetAsync(importedDataSet);

            redirectAttributes.addFlashAttribute("message", "Dataset ajouté avec succès : " + nom + ". L'importation est limitée à 500 lignes maximum. Le traitement se poursuit en arrière-plan.");
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        } catch (IllegalArgumentException e) {
            // Erreur de validation
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        } catch (IOException e) {
            // Erreur d'E/S
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Erreur lors de l'importation du fichier : " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        } catch (Exception e) {
            // Autres erreurs
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Erreur inattendue : " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        return "redirect:/admin/listDatasets";
    }
}
