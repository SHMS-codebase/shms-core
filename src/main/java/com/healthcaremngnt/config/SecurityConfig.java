package com.healthcaremngnt.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration File to bypass the default the Spring Security Login Page
 * Called before the SpringConfiguration Main Class
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

	private static final Logger logger = LogManager.getLogger(SecurityConfig.class);

	@Autowired
	private UserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		logger.info("Security Filter Chain");
		http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
				.requestMatchers("/", "/home", "/aboutus", "/contactus", "/services", "/public/**",
						"/auth/forgot-password", "/auth/forgot-password/email", "/auth/forgot-password/username",
						"/auth/reset-password", "/auth/reset-password-success", "/auth/reset-password/**", "/css/**",
						"/js/**", "/images/**") // to include static contents before logging in
				.permitAll().anyRequest().authenticated())
				.formLogin(formLogin -> formLogin.loginPage("/login").defaultSuccessUrl("/dashboard", true)
						// Redirect to /dashboard after successful login
						.permitAll())
				.logout(logout -> logout.permitAll())
				.rememberMe(rememberMe -> rememberMe.key("REMEMBER_ME_KEY").tokenValiditySeconds(60 * 60 * 24) // 24 hours
				).csrf(csrf -> csrf.disable()); // Disable CSRF for simplicity

		return http.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		logger.debug("Authentication Provider to validate User Login Details");
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		logger.debug("Using BCrypt to iterate Password by 12 rounds");
		provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
		// pass the customized userDetailsService to work with DB
		provider.setUserDetailsService(userDetailsService);
		return provider;
	}

}