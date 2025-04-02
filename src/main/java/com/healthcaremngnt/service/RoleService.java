package com.healthcaremngnt.service;

import java.util.List;
import java.util.Optional;

import com.healthcaremngnt.model.Role;

public interface RoleService {

	Optional<Role> getRoleByName(String roleName);

	List<Role> getAllRoles();

}