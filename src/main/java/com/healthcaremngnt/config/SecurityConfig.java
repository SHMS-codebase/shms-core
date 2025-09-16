package com.healthcaremngnt.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

	private final UserDetailsService userDetailsService;

	public SecurityConfig(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Defines the main security filter chain configuration.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		logger.info("Configuring Spring Security for Smart Healthcare Management Application");
		
		// Define URL patterns and their access rules		
		http
			.authorizeHttpRequests(authorizeRequests -> authorizeRequests
					.requestMatchers(
							"/", "/home", "/aboutus", "/contactus", "/services", 
							"/public/**",
							"/auth/forgot-password", "/auth/forgot-password/email", 
							"/auth/forgot-password/username", "/auth/reset-password", 
							"/auth/reset-password-success", "/auth/reset-password/**", 
							"/css/**", "/js/**", "/images/**" // to include static contents before logging in --> not needed in latest Spring Security
					).permitAll()
					.anyRequest().authenticated()
			)
			.formLogin(formLogin -> formLogin
					.loginPage("/login")
					.defaultSuccessUrl("/dashboard", true) // Redirect to /dashboard after successful login
					.permitAll()
			)
			.logout(logout -> logout
					.logoutUrl("/logout")
					.logoutSuccessUrl("/login?logout")
					.invalidateHttpSession(true)
					.deleteCookies("JSESSIONID", "remember-me")
					.permitAll()
			)
			.rememberMe(rememberMe -> rememberMe
					.key("REMEMBER_ME_KEY")
					.tokenValiditySeconds(60 * 60 * 24) // 24 hours
					.userDetailsService(userDetailsService)
			);
		
		// csrf is enabled by default in Spring Security 5.7+

		return http.build();
	}

	/**
     * Configures the authentication provider with custom user details service and password encoder.
     */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		
		logger.debug("Authentication Provider to validate User Login Details");
		
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(userDetailsService); // pass the customized userDetailsService to work with DB
		
		return provider;
	}
	
	 /**
     * Exposes AuthenticationManager bean for programmatic authentication if needed.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Password encoder bean using BCrypt with strength 12.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.debug("Using BCryptPasswordEncoder with strength 12");
        return new BCryptPasswordEncoder(12);
    }

}