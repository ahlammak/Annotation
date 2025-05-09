package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
      User findByLogin(String username);
      Page<User> findByNomContainsIgnoreCaseOrPrenomContainsIgnoreCase(String nom, String prenom , Pageable pageable);

}
