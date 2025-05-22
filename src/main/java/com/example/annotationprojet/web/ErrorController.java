package com.example.annotationprojet.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = "Une erreur s'est produite";
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                errorMessage = "La page demandée n'existe pas";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                errorMessage = "Vous n'avez pas les droits pour accéder à cette page";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                errorMessage = "Une erreur interne s'est produite sur le serveur";
            }
        }
        
        model.addAttribute("errorMessage", errorMessage);
        return "error/custom-error";
    }
}
