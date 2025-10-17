package com.healthcaremngnt.controller;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.healthcaremngnt.constants.RequestParamConstants;

@Controller
@RequestMapping("/api/v1/payment")
public class PaymentController {

	private static final Logger logger = LogManager.getLogger(PaymentController.class);

	public PaymentController() {

	}

	@GetMapping("/recentpayments")
	public String viewRecentVisits(@RequestParam(RequestParamConstants.PATIENT_ID) Long patientID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading Recent Payments of the Patient ID: {}", patientID);

		model.addAttribute("patientID", patientID);
		model.addAttribute("source", source);

		return "recentpayments";
	}

	@GetMapping("/printreceipt")
	public String printPaymentReceipt(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
	
		logger.info("Loading Print Payment Receipt Page");
		
		String currentDate = LocalDateTime.now().toString().replace("T", " ");

		model.addAttribute("source", source);
		model.addAttribute("currentDate", currentDate);
		return "printreceipt";
	}

}