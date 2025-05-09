package com.example.annotationprojet.web.admin;

import com.example.annotationprojet.entities.Annotateur;
import com.example.annotationprojet.entities.Role;
import com.example.annotationprojet.repositories.AnnotateurRepository;
import com.example.annotationprojet.repositories.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Random;

@Controller
public class AdminController {

    @Autowired
    private AnnotateurRepository annotateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String generateRandomPassword() {
        String digits = "0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        int length = 4;

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(digits.length());
            sb.append(digits.charAt(index));
        }
        return sb.toString();
    }
    @GetMapping("/admin/addAnnotateur")
    public String addAnnotateur(Model model) {
        model.addAttribute("user", new Annotateur());
        return "admin/GererAnnotateur/addAnnotateur";
    }

    @PostMapping(path = "/save")
    public String save(Model model,
                       Annotateur annotateur,
                       BindingResult bindingResult,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "5") int size,
                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/GererAnnotateur/editUsers";
        }

        String clearPassword = null;

        if (annotateur.getID() != null && annotateur.getID() > 0) {
            clearPassword = generateRandomPassword();
            Annotateur existing = annotateurRepository.findById(annotateur.getID())
                    .orElseThrow(() -> new RuntimeException("Annotateur non trouvé"));

            existing.setNom(annotateur.getNom());
            existing.setPrenom(annotateur.getPrenom());
            existing.setLogin(annotateur.getLogin());

            // S'assurer que l'annotateur a un rôle
            if (existing.getRole() == null) {
                Role roleUser = roleRepository.findByNomRole("USER");
                if (roleUser == null) {
                    roleUser = new Role();
                    roleUser.setNomRole("USER");
                    roleRepository.save(roleUser);
                }
                existing.setRole(roleUser);
            }

            if (annotateur.getPassword() != null && !annotateur.getPassword().isEmpty()) {
                existing.setPassword(passwordEncoder.encode(clearPassword));
            }

            annotateurRepository.save(existing);
        } else {
            // Génération d'un mot de passe aléatoire à 4 chiffres
            clearPassword = generateRandomPassword();
            annotateur.setActive(true);

            // Assigner le rôle USER à l'annotateur
            Role roleUser = roleRepository.findByNomRole("USER");
            if (roleUser == null) {
                // Si le rôle n'existe pas, le créer
                roleUser = new Role();
                roleUser.setNomRole("USER");
                roleRepository.save(roleUser);
            }
            annotateur.setRole(roleUser);

            // Hashage du mot de passe
            annotateur.setPassword(passwordEncoder.encode(clearPassword));

            annotateurRepository.save(annotateur);
        }

        if (clearPassword != null) {
            redirectAttributes.addFlashAttribute("generatedPassword", clearPassword);
            redirectAttributes.addFlashAttribute("newUser", annotateur);
        }

        return "redirect:/admin/ListeAnnotateur?page=" + page + "&keyword=" + keyword + "&size=" + size;
    }

    @GetMapping("/admin/ListeAnnotateur")
    public String index(Model model,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "5") int size,
                       @RequestParam(name = "keyword", defaultValue = "") String keyword) {


        Page<Annotateur> pageAnnotateur;
        if (keyword != null && !keyword.isEmpty()) {
            // Utiliser le même mot-clé pour la recherche dans le nom et le prénom
            pageAnnotateur = annotateurRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseAndActiveTrue(
                    keyword, keyword, PageRequest.of(page, size));
        } else {
            // Trouver tous les annotateurs actifs
            pageAnnotateur = annotateurRepository.findByActiveTrue(PageRequest.of(page, size));
        }

        model.addAttribute("listeAnnotateur", pageAnnotateur.getContent());
        model.addAttribute("pages", new int[pageAnnotateur.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        model.addAttribute("totalItems", pageAnnotateur.getTotalElements());
        model.addAttribute("totalPages", pageAnnotateur.getTotalPages());

        return "admin/GererAnnotateur/ListeAnnotateur";
    }

    @GetMapping("/editUsers")
    public String edit(Model model,
                       @RequestParam int id,
                       @RequestParam String keyword,
                       @RequestParam int page,
                       @RequestParam(defaultValue = "5") int size) {
        Annotateur annotateur = annotateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotateur non trouvé"));
        model.addAttribute("user", annotateur);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/GererAnnotateur/editUsers";
    }

    @GetMapping("/deleteUser")
    @Transactional
    public String delete(@RequestParam(name = "id") int id,
                         @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size) {
        Annotateur annotateur = annotateurRepository.findById(id).orElse(null);
        if (annotateur != null) {
            annotateur.setActive(false);
            annotateurRepository.saveAndFlush(annotateur);
        }
        return "redirect:/admin/ListeAnnotateur?page="+page+"&keyword="+keyword+"&size="+size;
    }
}


