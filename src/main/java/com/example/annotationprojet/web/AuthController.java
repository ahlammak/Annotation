package com.example.annotationprojet.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/check")
    public Map<String, Object> checkAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        
        response.put("authenticated", auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser"));
        response.put("username", auth.getName());
        response.put("roles", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        return response;
    }
}
