package com.healthcaremngnt.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.service.NotificationService;

@Controller
@RequestMapping("/api/v1/notifications")
public class NotificationController {

	private static final Logger logger = LogManager.getLogger(AppointmentController.class);

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("/systemalerts")
	public String viewSystemAlerts(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Viewing system alerts");

		model.addAttribute("source", source);
		return "systemalerts";
	}

	@GetMapping("/adminnotifications")
	public String viewAdminNotifications(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Viewing admin notifications");

		model.addAttribute("source", source);
		return "adminnotifications";
	}

}