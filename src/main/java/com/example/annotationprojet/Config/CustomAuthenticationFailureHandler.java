package com.example.annotationprojet.Config;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        System.out.println("Échec d'authentification: " + exception.getMessage());
        exception.printStackTrace();


        String username = request.getParameter("username");
        System.out.println("Tentative de connexion avec le nom d'utilisateur: " + username);

        if (exception instanceof DisabledException) {

            response.sendRedirect("login?error=disabled");
            return;
        }

        else if (exception instanceof LockedException) {

            response.sendRedirect("login?error=locked");
            return;
        }

        else if (exception instanceof CredentialsExpiredException) {

            response.sendRedirect("login?error=expired");
            return;
        }

        else {

            response.sendRedirect("login?error=other&message=" + exception.getMessage());
        }
    }
}
