package com.healthcaremngnt.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.exceptions.DoctorNotFoundException;
import com.healthcaremngnt.exceptions.PatientNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

	private static final Logger logger = LogManager.getLogger(AuthenticationController.class);

	private final DoctorController doctorController;
	private final PatientController patientController;

	public AuthenticationController(DoctorController doctorController, PatientController patientController) {
		this.doctorController = doctorController;
		this.patientController = patientController;
	}

	@GetMapping("/login")
	public String login(@RequestParam(value = RequestParamConstants.ERROR, required = false) String error,
			Model model) {
		logger.info("Login Form being loaded");
		if (error != null) {
			logger.debug("Error from URL: {}", error);
			model.addAttribute("errorMessage", MessageConstants.LOGIN_ERROR);
		}
		return "login";
	}

	@GetMapping("/dashboard")
	public String dashboardRedirect(Authentication authentication, Model model, HttpSession session) {
		
		logger.info("Login Successful. Dashboard Redirection in progress based on User Role");
		
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		if (userDetails != null) {
			logger.debug("User Name: {}", userDetails::getUsername);
			logger.debug("User Authorities/Role: {}", userDetails::getAuthorities);

			// Using a stream to extract roles and join them as a single string
			var roleName = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
					.collect(Collectors.joining(", "));

			model.addAttribute("userName", userDetails.getUsername());
			model.addAttribute("roleName", roleName);

			session.setAttribute("userName", userDetails.getUsername());
			session.setAttribute("roleName", roleName);
		}

	    var role = authentication.getAuthorities().stream()
	        .map(GrantedAuthority::getAuthority)
	        .filter(auth -> List.of("ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_PATIENT").contains(auth.toUpperCase()))
	        .findFirst().orElse("HOME");

	    return switch (role.toUpperCase()) {
	        case "ROLE_ADMIN" -> "redirect:/api/v1/auth/dashboard/admin";
	        case "ROLE_DOCTOR" -> "redirect:/api/v1/auth/dashboard/doctor";
	        case "ROLE_PATIENT" -> "redirect:/api/v1/auth/dashboard/patient";
	        default -> "home";
	    };
	}
	
	@GetMapping("/dashboard/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminDashboard(Authentication authentication, Model model) {
		
		logger.info("Admin Dashboard being loaded");
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	    model.addAttribute("userName", userDetails.getUsername());
	    model.addAttribute("roleName", "ADMIN");
		return "admindashboard";
	}

	@GetMapping("/dashboard/doctor")
	@PreAuthorize("hasRole('DOCTOR')")
	public String doctorDashboard(Authentication authentication, Model model) throws DoctorNotFoundException {
		
		logger.info("Doctor Dashboard being loaded");
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	    model.addAttribute("userName", userDetails.getUsername());
	    model.addAttribute("roleName", "DOCTOR");
	    return doctorController.getDoctorDashboard(authentication.getName(), model);
	}

	@GetMapping("/dashboard/patient")
	@PreAuthorize("hasRole('PATIENT')")
	public String patientDashboard(Authentication authentication, Model model) throws PatientNotFoundException {
		
		logger.info("Patient Dashboard being loaded");
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	    model.addAttribute("userName", userDetails.getUsername());
	    model.addAttribute("roleName", "PATIENT");
	    return patientController.getPatientDashboard(authentication.getName(), model);
	}
	
//	@GetMapping("/dashboard")
//	public String dashboard(Authentication authentication, Model model, HttpSession session)
//			throws DoctorNotFoundException, PatientNotFoundException {
//		logger.info("Login Successful and displaying the respective Dashboard Page");
//		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//
//		if (userDetails != null) {
//			logger.debug("User Name: {}", userDetails::getUsername);
//			logger.debug("User Authorities/Role: {}", userDetails::getAuthorities);
//
//			// Using a stream to extract roles and join them as a single string
//			var roleName = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
//					.collect(Collectors.joining(", "));
//
//			model.addAttribute("userName", userDetails.getUsername());
//			model.addAttribute("roleName", roleName);
//
//			session.setAttribute("userName", userDetails.getUsername());
//			session.setAttribute("roleName", roleName);
//		}
//
//		var role = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
//				.filter(authority -> List.of("ADMIN", "DOCTOR", "PATIENT").contains(authority.toUpperCase()))
//				.findFirst().orElse("HOME");
//
//		return switch (role.toUpperCase()) {
//		case "ADMIN" -> "admindashboard";
//		case "DOCTOR" -> doctorController.getDoctorDashboard(userDetails.getUsername(), model);
//		case "PATIENT" -> patientController.getPatientDashboard(userDetails.getUsername(), model);
//		default -> "home";
//		};
//
//	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		logger.info("Logging out of the Application");

		HttpSession session = request.getSession(false);
		if (session != null) {
			logger.debug("The session is being invalidated");
			session.invalidate(); // Invalidate the session
		}

		logger.debug("Redirecting to Login Page by default after logging out");
		return "redirect:/api/v1/auth/login";
	}

}