package com.example.annotationprojet.web.admin;
import com.example.annotationprojet.Service.AffectationAnnotateurService;
import com.example.annotationprojet.Service.AsyncDataSetService;
import com.example.annotationprojet.Service.DataSetService;
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
import java.util.HashSet;
import java.util.List;
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
        // Récupérer tous les datasets pour la liste
        List<DataSet> datasets = dataSetService.getAllDataSets();
        model.addAttribute("datasets", datasets);
        return "admin/Dataset/ListDatasets";
    }

    @GetMapping("/admin/viewDataset")
    public String viewDataset(@RequestParam("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Début de la méthode viewDataset avec id = " + id);

            // Récupérer le dataset
            DataSet dataSet = dataSetService.getDataSetById(id);
            if (dataSet == null) {
                System.out.println("Dataset introuvable avec id = " + id);
                redirectAttributes.addFlashAttribute("message", "Dataset introuvable.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/Dataset";
            }

            System.out.println("Dataset trouvé : " + dataSet.getNom());

            // Ajouter le dataset au modèle
            model.addAttribute("dataset", dataSet);

            // Récupérer les couples de textes associés au dataset
            List<coupleTexte> coupleTextes = new ArrayList<>();
            try {
                coupleTextes = coupleTexteRepository.findByDataSet(dataSet);
                System.out.println("Nombre de couples de textes récupérés : " + coupleTextes.size());
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération des couples de textes : " + e.getMessage());
                e.printStackTrace();
            }
            model.addAttribute("coupleTextes", coupleTextes);

            // Récupérer tous les annotateurs actifs
            List<Annotateur> annotateurs = new ArrayList<>();
            try {
                annotateurs = annotateurRepository.findByActiveTrue();
                System.out.println("Nombre d'annotateurs actifs récupérés : " + annotateurs.size());
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération des annotateurs : " + e.getMessage());
                e.printStackTrace();
            }
            model.addAttribute("annotateurs", annotateurs);

            // Récupérer les annotateurs déjà affectés à des tâches pour ce dataset
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

    /**
     * Supprime un annotateur d'un dataset en supprimant toutes ses tâches associées
     * @param datasetId L'ID du dataset
     * @param annotateurId L'ID de l'annotateur à supprimer
     * @param redirectAttributes Les attributs de redirection
     * @return La redirection vers la page de détail du dataset
     */
    @GetMapping("/admin/removeAnnotateurFromDataset")
    public String removeAnnotateurFromDataset(
            @RequestParam("datasetId") Integer datasetId,
            @RequestParam("annotateurId") Integer annotateurId,
            RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Suppression de l'annotateur " + annotateurId + " du dataset " + datasetId);

            // Récupérer le dataset
            DataSet dataset = dataSetService.getDataSetById(datasetId);
            if (dataset == null) {
                redirectAttributes.addFlashAttribute("message", "Dataset introuvable.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/Dataset";
            }

            // Récupérer l'annotateur
            Annotateur annotateur = annotateurRepository.findById(annotateurId).orElse(null);
            if (annotateur == null) {
                redirectAttributes.addFlashAttribute("message", "Annotateur introuvable.");
                redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                return "redirect:/admin/viewDataset?id=" + datasetId;
            }

            // Supprimer toutes les tâches de cet annotateur pour ce dataset
            List<Tache> taches = tacheRepository.findByDataAndAnnotateur(dataset, annotateur);
            System.out.println("Nombre de tâches à supprimer : " + (taches != null ? taches.size() : "null"));

            if (taches != null && !taches.isEmpty()) {
                tacheRepository.deleteAll(taches);
                System.out.println("Tâches supprimées avec succès");
            }

            redirectAttributes.addFlashAttribute("message", "Annotateur " + annotateur.getPrenom() + " " + annotateur.getNom() + " supprimé du dataset avec succès.");
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
