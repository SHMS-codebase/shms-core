package com.healthcaremngnt.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.model.Role;
import com.healthcaremngnt.repository.RoleRepository;
import com.healthcaremngnt.service.RoleService;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

	private static final Logger logger = LogManager.getLogger(RoleServiceImpl.class);

	private final RoleRepository roleRepository;

	public RoleServiceImpl(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@Override
	public Role getRoleByName(String roleName) {
		logger.info("Retrieving role by name: {}", roleName);

		validateRoleName(roleName);

		return roleRepository.findByRoleNameIgnoreCase(roleName)
				.orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
	}

	private void validateRoleName(String roleName) {
		if (roleName == null || roleName.isBlank()) {
			throw new IllegalArgumentException("Role name cannot be null or empty.");
		}
	}

	@Override
	public List<Role> getAllRoles() {
		logger.info("Fetching all roles from database.");
		
		return roleRepository.findAll();
	}

}