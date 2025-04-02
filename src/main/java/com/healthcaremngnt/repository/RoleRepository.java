package com.healthcaremngnt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

	Optional<Role> findByRoleNameIgnoreCase(String roleName);
	
	Role findByRoleID(int roleID);
	// If findByRoleID is just an alias for findById, remove it and use the inherited findById
    // Optional<Role> findByRoleID(int roleID); <- Remove if redundant, use findById

}