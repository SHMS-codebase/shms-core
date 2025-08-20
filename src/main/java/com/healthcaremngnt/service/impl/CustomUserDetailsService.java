package com.healthcaremngnt.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.healthcaremngnt.model.UserPrincipal;
import com.healthcaremngnt.repository.RoleRepository;
import com.healthcaremngnt.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private static final Logger logger = LogManager.getLogger(CustomUserDetailsService.class);

	private final UserRepository userRepository;

	private RoleRepository roleRepository;

	public CustomUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		logger.info("Authenticating user: {}", userName);

		var user = userRepository.findByUserName(userName).orElseThrow(() -> {
			logger.error("User not found: {}", userName);
			return new UsernameNotFoundException("User not found: " + userName);
		});

		return new UserPrincipal(user, roleRepository);
	}

}