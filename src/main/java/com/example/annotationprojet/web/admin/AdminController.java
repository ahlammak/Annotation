package com.example.annotationprojet.web.admin;

import com.example.annotationprojet.Service.AffectationAnnotateurService;
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

    @Autowired
    private AffectationAnnotateurService affectationAnnotateurService;

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
        return "admin/GererAnnotateur/addAnnotateur-standalone";
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

            clearPassword = generateRandomPassword();
            annotateur.setActive(true);


            Role roleUser = roleRepository.findByNomRole("USER");
            if (roleUser == null) {

                roleUser = new Role();
                roleUser.setNomRole("USER");
                roleRepository.save(roleUser);
            }
            annotateur.setRole(roleUser);

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
        return listeAnnotateur(model, page, size, keyword);
    }

    @GetMapping("/admin/listeAnnotateur")
    public String listeAnnotateurMinuscule(Model model,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "5") int size,
                       @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        return listeAnnotateur(model, page, size, keyword);
    }

    private String listeAnnotateur(Model model, int page, int size, String keyword) {
        Page<Annotateur> pageAnnotateur;
        if (keyword != null && !keyword.isEmpty()) {
            pageAnnotateur = annotateurRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseAndActiveTrue(
                    keyword, keyword, PageRequest.of(page, size));
        } else {
            pageAnnotateur = annotateurRepository.findByActiveTrue(PageRequest.of(page, size));
        }

        model.addAttribute("listeAnnotateur", pageAnnotateur.getContent());
        model.addAttribute("pages", new int[pageAnnotateur.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        model.addAttribute("totalItems", pageAnnotateur.getTotalElements());
        model.addAttribute("totalPages", pageAnnotateur.getTotalPages());

        return "admin/GererAnnotateur/ListeAnnotateur-standalone";
    }

    @GetMapping("/admin/editAnnotateur")
    public String editAnnotateur(Model model,
                       @RequestParam int id,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size) {
        return edit(model, id, keyword, page, size);
    }

    @GetMapping("/editUsers")
    public String edit(Model model,
                       @RequestParam int id,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size) {
        Annotateur annotateur = annotateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotateur non trouvé"));
        model.addAttribute("user", annotateur);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/GererAnnotateur/editUsers-standalone";
    }

    @GetMapping("/admin/deleteAnnotateur")
    public String deleteAnnotateur(@RequestParam(name = "id") int id,
                         @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size) {
        return delete(id, keyword, page, size);
    }

    @GetMapping("/deleteUser")
    @Transactional
    public String delete(@RequestParam(name = "id") int id,
                         @RequestParam(defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size) {
        Annotateur annotateur = annotateurRepository.findById(id).orElse(null);
        if (annotateur != null) {
            try {
                // Réaffecter les tâches de l'annotateur à d'autres annotateurs
                int reassignedCount = affectationAnnotateurService.reassignTasksForAllDatasets(annotateur);
                System.out.println("Total de " + reassignedCount + " tâches réaffectées pour l'annotateur " + annotateur.getPrenom() + " " + annotateur.getNom());

                // Désactiver l'annotateur (ne pas le supprimer complètement)
                annotateur.setActive(false);
                annotateurRepository.saveAndFlush(annotateur);
            } catch (Exception e) {
                System.err.println("Erreur lors de la suppression de l'annotateur : " + e.getMessage());
                e.printStackTrace();
            }
        }
        return "redirect:/admin/listeAnnotateur?page="+page+"&keyword="+keyword+"&size="+size;
    }
}


