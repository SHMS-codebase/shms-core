package com.healthcaremngnt.service.impl;

import java.util.List;
import java.util.Optional;

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
	public Optional<Role> getRoleByName(String roleName) {

		logger.info("Retrieving Role ID for role name: {}", roleName);

		return roleRepository.findByRoleNameIgnoreCase(roleName);
	}

	@Override
	public List<Role> getAllRoles() {

		logger.info("Retrieving all roles from DB");

		return roleRepository.findAll();
	}

}