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
    // This method is called when authentication fails
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // Print the stack trace of the exception for debugging purposes
        System.out.println("Échec d'authentification: " + exception.getMessage());
        exception.printStackTrace();

        // Log the username that was used in the authentication attempt
        String username = request.getParameter("username");
        System.out.println("Tentative de connexion avec le nom d'utilisateur: " + username);

        // Custom logic can be added here to handle different types of authentication failures
        // Check if the exception is a DisabledException (user account is disabled)
        if (exception instanceof DisabledException) {
            // Redirect the user to the login page with a "disabled" error message
            response.sendRedirect("login?error=disabled");
            return;
        }
        // Check if the exception is a LockedException (user account is locked)
        else if (exception instanceof LockedException) {
            // Redirect the user to the login page with a "locked" error message
            response.sendRedirect("login?error=locked");
            return;
        }
        // Check if the exception is a CredentialsExpiredException (user credentials are expired)
        else if (exception instanceof CredentialsExpiredException) {
            // Redirect the user to the login page with an "expired" error message
            response.sendRedirect("login?error=expired");
            return;
        }
        // Catch any other types of authentication exceptions
        else {
            // Redirect the user to the login page with a generic "other" error message
            response.sendRedirect("login?error=other&message=" + exception.getMessage());
        }
    }
}
