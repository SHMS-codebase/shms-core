package com.healthcaremngnt.service;

import java.util.List;

import com.healthcaremngnt.model.Role;

public interface RoleService {

	Role getRoleByName(String roleName);

	List<Role> getAllRoles();

}