package com.example.annotationprojet.repositories;

import com.example.annotationprojet.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByNomRole(String nomRole);
}
