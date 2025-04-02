package com.healthcaremngnt.service.impl;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.healthcaremngnt.model.User;
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
        logger.info("Loading User By User Name for Authentication - CustomUserDetailsService");
        Optional<User> userOptional = userRepository.findByUserName(userName);
        
        User user = new User();
        if(userOptional.isPresent())
        	user = userOptional.get();
        
        if (user == null) {
            logger.error("User Not Found");
            throw new UsernameNotFoundException("User Not Found");
        }

        return new UserPrincipal(user, roleRepository);
    }

}