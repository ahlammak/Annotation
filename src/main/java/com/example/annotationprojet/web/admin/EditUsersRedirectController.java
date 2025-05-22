package com.example.annotationprojet.web.admin;

import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.repositories.AnnotateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/GererAnnotateur")
public class EditUsersRedirectController {

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @GetMapping("/editUsersRedirect")
    public String editUsersRedirect(
            @RequestParam(name = "id", defaultValue = "0") Integer id,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Model model) {
        
        // Récupérer l'utilisateur à partir de l'ID
        Annotateur user = annotateurRepository.findById(id).orElse(null);
        
        // Ajouter les attributs au modèle
        model.addAttribute("user", user);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        
        return "admin/GererAnnotateur/editUsersRedirect";
    }
}
