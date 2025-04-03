package com.healthcaremngnt.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.exceptions.AppointmentNotFoundException;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.Treatment;
import com.healthcaremngnt.model.TreatmentForm;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.TreatmentService;

@Controller
@RequestMapping("/treatments")
public class TreatmentController {

	private static final Logger logger = LogManager.getLogger(TreatmentController.class);

	private final TreatmentService treatmentService;
	private final AppointmentService appointmentService;

	public TreatmentController(TreatmentService treatmentService, AppointmentService appointmentService) {
		this.treatmentService = treatmentService;
		this.appointmentService = appointmentService;
	}

	@GetMapping("/createtreatment")
	public String viewCreateTreatment(
			@RequestParam(value = RequestParamConstants.APPOINTMENT_ID, required = false) Long appointmentID,
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source, Model model) {
		logger.info("Loading Create Treatment!!!");

		if (logger.isDebugEnabled()) {
			logger.debug("Request parameters - appointmentID: {}, source: {}",
					appointmentID != null ? appointmentID : "null", source != null ? source : "null");
		}

		// Add all attributes to model
		model.addAttribute("appointmentID", appointmentID);
		model.addAttribute("source", source);
		model.addAttribute("treatmentForm", new TreatmentForm());
		model.addAttribute("isSaved", model.containsAttribute("isSaved") ? model.getAttribute("isSaved") : false);

		return "createtreatment";
	}

	@PostMapping("/savetreatment")
	public String saveTreatment(
			@RequestParam(value = RequestParamConstants.APPOINTMENT_ID, required = false) Long appointmentID,
			@ModelAttribute TreatmentForm treatmentForm,
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source,
			@RequestParam(value = RequestParamConstants.CONTINUE_TREATMENT, required = false) Boolean continueTreatment,
			RedirectAttributes redirectAttributes, Model model, Authentication authentication)
			throws AppointmentNotFoundException {
		logger.info("Creating Treatment!!!");

		if (logger.isDebugEnabled()) {
			logger.debug("Request parameters - appointmentID: {}, source: {}, treatmentForm: {}", appointmentID, source,
					treatmentForm);
		}

		Treatment treatment = new Treatment();

		if (appointmentID != null) {
			try {
				Appointment appointment = appointmentService.getAppointmentDetails(appointmentID);
				treatment.setAppointment(appointment);
			} catch (AppointmentNotFoundException e) {
				logger.error("{}: {}", MessageConstants.APMNT_NOT_FOUND, e.getMessage());
				model.addAttribute("errorMessage", MessageConstants.APMNT_NOT_FOUND);
				return "createtreatment";
			}
		}

		if (treatmentForm != null) {
			treatment.setDiagnosis(treatmentForm.getDiagnosis());
			treatment.setTreatmentDetails(treatmentForm.getTreatmentDetails());
			treatment.setNotes(treatmentForm.getNotes());
			treatment.setFollowUpNeeded(treatmentForm.getFollowUpNeeded());
			treatment.setInvoiceGenerated(false);
			treatment.setTreatmentDate(LocalDate.now());
			treatment.setTreatmentStatus(treatmentForm.getTreatmentStatus());
		}

		try {
			// Save treatment
			Treatment savedTreatment = treatmentService.createTreatment(treatment);

			// When clicked on 'Create Treatment & Continue to Prescription' button
			if (continueTreatment != null && continueTreatment) {
				logger.info("Redirecting to Create Prescriptions!!!");
				if (appointmentID != null && savedTreatment != null && source != null) {
					redirectAttributes.addFlashAttribute("appointmentID", appointmentID);
					redirectAttributes.addFlashAttribute("savedTreatment", savedTreatment);
					redirectAttributes.addFlashAttribute("source", source);
					return "redirect:/prescriptions/createprescription"; // Redirect to createprescription page
				} else {
					logger.error("Missing required values for redirect: appointmentID={}, savedTreatment={}, source={}",
							appointmentID, savedTreatment, source);
					return "errorPage"; // Handle the error gracefully
				}
			}

			// Update appointment status
			appointmentService.updateAppointmentStatus(appointmentID, AppointmentStatus.COMPLETED);

			logger.debug("{}", MessageConstants.TREATMENT_PLAN_SUCCESS);

			model.addAttribute("treatmentForm", treatmentForm);
			model.addAttribute("message", MessageConstants.TREATMENT_PLAN_SUCCESS);
			model.addAttribute("isSaved", true);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.TREATMENT_PLAN_FAILURE, e);
			model.addAttribute("errorMessage", MessageConstants.TREATMENT_PLAN_FAILURE);
			model.addAttribute("isSaved", false);
		}

		model.addAttribute("source", source);

		return "createtreatment";

	}

	@GetMapping("/viewtreatment")
	public String viewTreatment(
			@RequestParam(value = RequestParamConstants.TREATMENT_ID, required = false) Long treatmentID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading View Treatment");
		Optional.ofNullable(treatmentID).ifPresent(id -> logger.debug("Treatment ID: {}", id));
		// or
		Optional.ofNullable(treatmentID).ifPresent(id -> {
			String maskedID = String.valueOf(id).replaceAll("(?<=.{2}).(?=.*$)", "*");
			logger.debug("Treatment ID: {}", maskedID);
		});

		try {
			Treatment treatment = treatmentService.getTreatmentDetails(treatmentID);

			logger.debug("treatment: {}", treatment);
			model.addAttribute("treatment", treatment);
		} catch (NumberFormatException e) {
			logger.error("{}: {}", MessageConstants.TREATMENT_INVALID_IDFORMAT, treatmentID, e);
			model.addAttribute("errorMessage", MessageConstants.TREATMENT_INVALID_IDFORMAT);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.TREATMENT_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.TREATMENT_LOAD_ERROR);
		}

		model.addAttribute("treatmentID", treatmentID);
		model.addAttribute("source", source);

		return "viewtreatment";
	}

	@PostMapping("/updatetreatment")
	public String updateTreatment(@ModelAttribute Treatment treatment,
			@RequestParam(value = RequestParamConstants.TREATMENT_ID, required = false) Long treatmentID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Updating Treatments");
		Optional.ofNullable(treatment).ifPresent(id -> logger.debug("treatment: {}", id));
		Optional.ofNullable(treatmentID).ifPresent(id -> logger.debug("Treatment ID", id));
		Optional.ofNullable(source).ifPresent(src -> logger.debug("Source: {}", src));

		try {
			treatmentService.updateTreatmentDetails(treatment);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.TREATMENT_UPDATE_FAILURE, e);
			model.addAttribute("errorMessage", MessageConstants.TREATMENT_UPDATE_FAILURE);
			model.addAttribute("treatment", treatment);
			return "viewtreatment";
		}

		logger.debug("{}", MessageConstants.TREATMENT_UPDATE_SUCCESS);

		model.addAttribute("treatment", treatment);
		model.addAttribute("treatmentID", treatmentID);
		model.addAttribute("message", MessageConstants.TREATMENT_UPDATE_SUCCESS);
		model.addAttribute("source", source);

		return "viewtreatment";
	}

}