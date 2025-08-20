package com.healthcaremngnt.model;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.healthcaremngnt.repository.RoleRepository;

public class UserPrincipal implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private User user;

	private RoleRepository roleRepository;

	public UserPrincipal(User user, RoleRepository roleRepository) {
		this.user = user;
		this.roleRepository = roleRepository;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

//		Integer roleID = user.getRole().getRoleID();
//		Optional<Role> roleOptional = roleRepository.findById(roleID);
//		String roleName = roleOptional.map(Role::getRoleName).orElse("USER");

		// LVTI (var) used
		var roleID = user.getRole().getRoleID();
		var roleOptional = roleRepository.findById(roleID);
		var roleName = roleOptional.map(Role::getRoleName).orElse("USER");

//		Map<Integer, String> roleMap = new HashMap<>();
//		roleMap.put(1, "ADMIN");
//		roleMap.put(2, "DOCTOR");
//		roleMap.put(3, "PATIENT");
//		String roleName = roleMap.getOrDefault(user.getRoleID(), "USER");

		return Collections.singleton(new SimpleGrantedAuthority(roleName));
	}

	@Override
	public String getUsername() {
		return user.getUserName();
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

}