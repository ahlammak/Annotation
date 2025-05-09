package com.example.annotationprojet.Service;

import com.example.annotationprojet.entities.CustomUserDetails;
import com.example.annotationprojet.entities.User;
import com.example.annotationprojet.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import jakarta.transaction.Transactional;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username);

        if (user == null) {
            throw new UsernameNotFoundException("Utilisateur introuvable par " + username);
        }

        if (user.getRole() == null) {
            throw new UsernameNotFoundException("Utilisateur sans droits d'accès");

        }

        return new CustomUserDetails(user);
    }

}
