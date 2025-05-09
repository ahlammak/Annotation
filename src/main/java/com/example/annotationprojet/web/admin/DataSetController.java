package com.example.annotationprojet.web.admin;

import com.example.annotationprojet.Service.DataSetService;
import com.example.annotationprojet.entities.Classes;
import com.example.annotationprojet.entities.DataSet;
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
import java.util.List;

@Controller
public class DataSetController {

    @Autowired
    private DataSetService dataSetService;

    @GetMapping("/admin/Dataset")
    public String showDatasetPage(Model model) {
        // Récupérer tous les datasets et les ajouter au modèle
        List<DataSet> datasets = dataSetService.getAllDataSets();
        model.addAttribute("datasets", datasets);
        return "admin/Dataset/Dataset";
    }

    @PostMapping("/admin/addDataset")
    public String addDataset(@RequestParam("nom") String nom,
                            @RequestParam("classes") String classes,
                            @RequestParam("description") String description,
                            RedirectAttributes redirectAttributes) {
        try {
            // Utiliser le service pour créer le dataset avec les classes
            DataSet savedDataSet = dataSetService.createDataSet(nom, description, classes);

            redirectAttributes.addFlashAttribute("message", "Dataset ajouté avec succès : " + nom);
            redirectAttributes.addFlashAttribute("alertClass", "alert-success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erreur lors de l'ajout du dataset : " + e.getMessage());
            redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        }

        return "redirect:/admin/Dataset";
    }

    @PostMapping("/admin/importDataset")
    public String importDataset(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            // Utiliser le service pour importer le fichier CSV
            DataSet importedDataSet = dataSetService.importCsvFile(file);

            redirectAttributes.addFlashAttribute("message", "Fichier importé avec succès : " + file.getOriginalFilename());
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
        }

        return "redirect:/admin/Dataset";
    }
}
