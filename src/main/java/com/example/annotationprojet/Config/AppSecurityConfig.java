package com.example.annotationprojet.Config;

import org.apache.catalina.security.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class AppSecurityConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppSecurityConfig.class);

    @Autowired
    private UserDetailsService userService;

    public AppSecurityConfig() {
        LOGGER.debug("AppSecurityConfig Initialisé");
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(){
        return new RedirectionAfterAuthenticationSuccessHandler();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler(){
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection for testing
                .authenticationProvider(authProvider()) // Use the custom AuthenticationProvider
                .authorizeHttpRequests(authz -> authz // Configure authorization for different URL patterns
                        .requestMatchers("/user/**").hasRole("USER") // Only allow users with the USER role for /user/** paths
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Only allow users with the ADMIN role for /admin/** paths
                        .requestMatchers("/webjars/**","/main.css", "/images/**", "/favicon.ico").permitAll() // Allow access to static resources
                        .anyRequest().permitAll() // Allow all other requests without restriction
                )
                .formLogin(form -> form // Configure login settings
                        .loginPage("/login") // Custom login page URL
                        .loginProcessingUrl("/login") // URL for processing login
                        .failureHandler(authenticationFailureHandler()) // Custom failure handler
                        .successHandler(authenticationSuccessHandler()) // Custom success handler
                        .permitAll() // Allow everyone to access the login page
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("JSESSIONID") // Ensures session cookies are removed
                        .invalidateHttpSession(true) // Invalidate the session
                        .logoutSuccessUrl("/login?logout=true") // Optionally, redirect to the login page on successful logout
                        .permitAll()
                )
                .exceptionHandling(exception -> exception // Configure exception handling
                        .accessDeniedPage("/access-denied") // Redirect users to /access-denied if they attempt to access unauthorized pages
                );
        return http.build(); // Build the security filter chain
    }
}