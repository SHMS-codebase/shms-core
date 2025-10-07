package com.healthcaremngnt.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer.SessionFixationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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

		// Security Headers
		http.headers(headers -> headers
				.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
				.contentTypeOptions(Customizer.withDefaults()).frameOptions(frame -> frame.deny())
				.xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)));

		// Access Rules
		http.authorizeHttpRequests(auth -> auth.requestMatchers("/view/", "/view/home", "/view/aboutus",
				"/view/contactus", "/view/services", "/public/**", "/api/v1/pwd/forgot-password",
				"/api/v1/pwd/forgot-password/email", "/api/v1/pwd/forgot-password/username",
				"/api/v1/pwd/reset-password", "/api/v1/pwd/reset-password-success", "/api/v1/pwd/reset-password/**",
				"/css/**", "/js/**", "/images/**").permitAll().anyRequest().authenticated());

		// Form Login
		http.formLogin(form -> form.loginPage("/api/v1/auth/login").defaultSuccessUrl("/api/v1/auth/dashboard", true)
				.permitAll());

		// Logout
		http.logout(logout -> logout.logoutUrl("/api/v1/auth/logout").logoutSuccessUrl("/api/v1/auth/login?logout")
				.invalidateHttpSession(true).deleteCookies("JSESSIONID", "remember-me").permitAll());

		// Remember Me
		http.rememberMe(remember -> remember.key("REMEMBER_ME_KEY").tokenValiditySeconds(60 * 60 * 24)
				.userDetailsService(userDetailsService));

		// CORS
		http.cors(Customizer.withDefaults());

		// CSRF: Disable for /api/**
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

		// Session Fixation
		http.sessionManagement(session -> session.sessionFixation(SessionFixationConfigurer::migrateSession));

		// Concurrent Sessions
		http.sessionManagement(
				session -> session.maximumSessions(3).maxSessionsPreventsLogin(false).expiredSessionStrategy(event -> {
					logger.debug("Session expired for: {}", event.getSessionInformation().getPrincipal());
				}));

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("http://localhost:8080"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	/**
	 * Configures the authentication provider with custom user details service and
	 * password encoder.
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

		logger.debug("Using DelegatingPasswordEncoder with Argon2id as default");

		String encodingID = "argon2";
		Map<String, PasswordEncoder> encoders = new HashMap<>();

		encoders.put("argon2", new Argon2PasswordEncoder(16, 32, 1, 65536, 3)); // memory=65536KB, parallelism=4
		encoders.put("bcrypt", new BCryptPasswordEncoder(12)); // strength=12

		return new DelegatingPasswordEncoder(encodingID, encoders);
	}

}