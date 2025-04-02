package com.healthcaremngnt.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

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

		Integer roleID = user.getRole().getRoleID();

		Optional<Role> roleOptional = roleRepository.findById(roleID);

//		Map<Integer, String> roleMap = new HashMap<>();
//		roleMap.put(1, "ADMIN");
//		roleMap.put(2, "DOCTOR");
//		roleMap.put(3, "PATIENT");

//		String roleName = roleMap.getOrDefault(user.getRoleID(), "USER");

		String roleName = roleOptional.map(Role::getRoleName).orElse("USER");

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