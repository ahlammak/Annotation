package com.example.annotationprojet.entities;
import com.example.annotationprojet.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Si ton utilisateur a un rôle, tu peux le récupérer et le convertir en un SimpleGrantedAuthority
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+user.getRole().getNomRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // Retourne le mot de passe de l'utilisateur
    }

    @Override
    public String getUsername() {
        return user.getLogin(); // Retourne le login de l'utilisateur
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // L'account n'est pas expiré (si tu as cette logique)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // L'account n'est pas verrouillé (si tu as cette logique)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Les credentials ne sont pas expirés (si tu as cette logique)
    }

    @Override
    public boolean isEnabled() {
        return true; // L'utilisateur est activé
    }

    // Tu peux ajouter d'autres méthodes utiles si nécessaire, par exemple pour obtenir l'utilisateur complet
    public User getUser() {
        return this.user;
    }
}
