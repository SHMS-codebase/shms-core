package com.healthcaremngnt.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.healthcaremngnt.constants.RequestParamConstants;

@Controller
@SessionAttributes("userName")
@RequestMapping("/view")
public class HomeController {

	private static final Logger logger = LogManager.getLogger(HomeController.class);

	public HomeController() {
		logger.debug("HomeController Constructor");
	}

	@GetMapping("/home")
	public String home() {
		logger.info("Home link is being clicked");
		return "home";
	}
	
	@GetMapping("/")
	public String launchHome() {
		logger.info("Initial Application Launch!!!");
		return "redirect:/api/v1/auth/login";
	}

	@GetMapping("/buttonplatter")
	public String viewButtonPlatter(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Viewing Button Platter!!!");
		model.addAttribute("source", source);
		return "buttonplatter";
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

}