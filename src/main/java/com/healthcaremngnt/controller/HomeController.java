package com.healthcaremngnt.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@SessionAttributes("userName")
public class HomeController {

	private static final Logger logger = LogManager.getLogger(HomeController.class);

	private final DoctorController doctorController;
	private final PatientController patientController;

	public HomeController(DoctorController doctorController, PatientController patientController) {
		this.doctorController = doctorController;
		this.patientController = patientController;
		logger.debug("HomeController Constructor");
	}

	@GetMapping("/home")
	public String home() {
		logger.info("Home link is being clicked");
		return "home";
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
	public String dashboard(Authentication authentication, Model model, HttpSession session) {
		logger.info("Login Successful and displaying the respective Dashboard Page");
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		if (userDetails != null) {
			logger.debug("User Name: {}", userDetails::getUsername);
			logger.debug("User Authorities/Role: {}", userDetails::getAuthorities);

			// Using a stream to extract roles and join them as a single string
			String roleName = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
					.collect(Collectors.joining(", "));

			model.addAttribute("userName", userDetails.getUsername());
			model.addAttribute("roleName", roleName);
			
			session.setAttribute("userName", userDetails.getUsername());
			session.setAttribute("roleName", roleName);
		}

		String role = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.filter(authority -> List.of("ADMIN", "DOCTOR", "PATIENT").contains(authority.toUpperCase()))
				.findFirst().orElse("HOME");
		
		return switch (role.toUpperCase()) {
		case "ADMIN" -> "admindashboard";
		case "DOCTOR" -> doctorController.getDoctorDashboard(userDetails.getUsername(), model);
		case "PATIENT" -> patientController.getPatientDashboard(userDetails.getUsername(), model);
		default -> "home";
		};

	}

	@GetMapping("/admindashboard")
	public String admindashboard() {
		logger.info("The Administrator DashBoard is being loaded");
		return "admindashboard";
	}

	@GetMapping("/services")
	public String viewServices() {
		logger.info("The Services page is being loaded");
		return "services";
	}

	@GetMapping("/aboutus")
	public String viewAboutUs() {
		logger.info("The About Us page is being loaded");
		return "aboutus";
	}

	@GetMapping("/contactus")
	public String viewContactUs() {
		logger.info("The Contact Us page is being loaded");
		return "contactus";
	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		logger.info("Logging out of the Application");

		HttpSession session = request.getSession(false);
		if (session != null) {
			logger.debug("The session is being invalidated");
			session.invalidate(); // Invalidate the session
		}

		logger.debug("Redirecting to Login Page by default after logging out");
		return "redirect:/login";
	}

}