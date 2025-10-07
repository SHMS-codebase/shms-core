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
import com.healthcaremngnt.exceptions.PrescriptionNotFoundException;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.Treatment;
import com.healthcaremngnt.model.TreatmentForm;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.DoctorService;
import com.healthcaremngnt.service.PatientService;
import com.healthcaremngnt.service.PrescriptionService;
import com.healthcaremngnt.service.TreatmentService;

@Controller
@RequestMapping("/api/v1/treatments")
public class TreatmentController {

	private static final Logger logger = LogManager.getLogger(TreatmentController.class);

	private final TreatmentService treatmentService;
	private final PrescriptionService prescriptionService;
	private final AppointmentService appointmentService;
	private final DoctorService doctorService;
	private final PatientService patientService;

	public TreatmentController(TreatmentService treatmentService, PrescriptionService prescriptionService,
			AppointmentService appointmentService, DoctorService doctorService, PatientService patientService) {

		this.treatmentService = treatmentService;
		this.prescriptionService = prescriptionService;
		this.appointmentService = appointmentService;
		this.doctorService = doctorService;
		this.patientService = patientService;
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
					return "redirect:/api/v1/prescriptions/createprescription"; // Redirect to createprescription page
				} else {
					logger.error("Missing required values for redirect: appointmentID={}, savedTreatment={}, source={}",
							appointmentID, savedTreatment, source);
					return "errorPage"; // Handle the error gracefully
				}
			}

			if (savedTreatment != null) {
				// Update appointment with treatment details and appointment status
				logger.debug("savedTreatment.getTreatmentID(): {}", savedTreatment.getTreatmentID());
				logger.debug("savedTreatment.getDiagnosis(): {}", savedTreatment.getDiagnosis());
				logger.debug("savedTreatment.getDoctorID(): {}", savedTreatment.getDoctorID());
				logger.debug("savedTreatment.getFollowUpNeeded(): {}", savedTreatment.getFollowUpNeeded());
				logger.debug("savedTreatment.getInvoiceGenerated(): {}", savedTreatment.getInvoiceGenerated());
				logger.debug("savedTreatment.getNotes(): {}", savedTreatment.getNotes());
				logger.debug("savedTreatment.getPatientID(): {}", savedTreatment.getPatientID());
				logger.debug("savedTreatment.getTreatmentDetails(): {}", savedTreatment.getTreatmentDetails());
				logger.debug("savedTreatment.getAppointment(): {}", savedTreatment.getAppointment());
				logger.debug("savedTreatment.getCreatedDate(): {}", savedTreatment.getCreatedDate());
				logger.debug("savedTreatment.getTreatmentDate(): {}", savedTreatment.getTreatmentDate());
				logger.debug("savedTreatment.getTreatmentStatus(): {}", savedTreatment.getTreatmentStatus());
				logger.debug("savedTreatment.getUpdatedDate(): {}", savedTreatment.getUpdatedDate());

				appointmentService.updateAppointmentStatusAndTreatment(appointmentID, AppointmentStatus.COMPLETED,
						savedTreatment);

				logger.debug("{}", MessageConstants.TREATMENT_PLAN_SUCCESS);

			}

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

	@GetMapping("/createfollowuptreatment")
	public String viewCrateFollowupTreatment(
			@RequestParam(value = RequestParamConstants.APPOINTMENT_ID, required = false) Long appointmentID,
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source, Model model) {

		logger.info("Loading Create Follow-up Treatment!!!");

		if (logger.isDebugEnabled()) {
			logger.debug("Request parameters - appointmentID: {}, source: {}",
					appointmentID != null ? appointmentID : "null", source != null ? source : "null");
		}

		if (appointmentID == null) {
			logger.warn("Appointment ID is null, cannot fetch previous treatment details");
			addModelAttributes(model, appointmentID, source);
			return "createfollowuptreatment";
		}

		try {
			loadPreviousTreatmentDetails(appointmentID, model);
		} catch (AppointmentNotFoundException e) {
			logger.error("{}: {}", MessageConstants.APMNT_NOT_FOUND, e.getMessage());
			model.addAttribute("errorMessage", MessageConstants.APMNT_NOT_FOUND);
		}

		addModelAttributes(model, appointmentID, source);
		return "createfollowuptreatment";
	}

	private void loadPreviousTreatmentDetails(Long appointmentID, Model model) throws AppointmentNotFoundException {

		Appointment appointment = appointmentService.getAppointmentDetails(appointmentID);
		if (appointment == null || appointment.getParentAppointment() == null) {
			logger.warn("Appointment or parent appointment not found for ID: {}", appointmentID);
			return;
		}

		Appointment previousAppointment = appointmentService
				.getAppointmentDetails(appointment.getParentAppointment().getAppointmentID());

		if (previousAppointment == null || previousAppointment.getTreatment() == null) {
			logger.warn("No previous appointment or treatment found for appointment ID: {}", appointmentID);
			return;
		}

		Treatment previousTreatment = previousAppointment.getTreatment();
		logger.debug("Previous Treatment Details: {}", previousTreatment);
		model.addAttribute("previousTreatment", previousTreatment);

		Long doctorID = previousTreatment.getDoctorID();
		Long patientID = previousTreatment.getPatientID();

		if (doctorID != null) {
			String doctorName = doctorService.getDoctorNameByID(doctorID);
			model.addAttribute("doctorName", doctorName);
		} else {
			logger.warn("Doctor ID is null in previous treatment for appointment ID: {}", appointmentID);
		}

		if (patientID != null) {
			String patientName = patientService.getPatientNameByID(patientID);
			model.addAttribute("patientName", patientName);
		} else {
			logger.warn("Patient ID is null in previous treatment for appointment ID: {}", appointmentID);
		}

		loadPreviousPrescription(previousTreatment, model);

	}

	private void loadPreviousPrescription(Treatment previousTreatment, Model model) {
		if (previousTreatment == null || previousTreatment.getTreatmentID() == null) {
			logger.warn("Previous treatment or treatment ID is null");
			return;
		}

		try {
			Prescription prescription = prescriptionService
					.getPrescriptionDetailsByTreatment(previousTreatment.getTreatmentID());

			if (prescription != null) {
				logger.debug("Previous Prescription Details: {}", prescription);
				model.addAttribute("previousPrescription", prescription);
			} else {
				logger.info("No previous prescription found for treatment ID: {}", previousTreatment.getTreatmentID());
			}
		} catch (PrescriptionNotFoundException e) {
			logger.info("No prescription available for treatment ID: {}. Reason: {}",
					previousTreatment.getTreatmentID(), e.getMessage());
		}
	}

	private void addModelAttributes(Model model, Long appointmentID, String source) {
		model.addAttribute("appointmentID", appointmentID);
		model.addAttribute("source", source);
		model.addAttribute("treatmentForm", new TreatmentForm());
		model.addAttribute("isSaved", model.containsAttribute("isSaved") ? model.getAttribute("isSaved") : false);
	}

	@GetMapping("/viewtreatment")
	public String viewTreatment(
			@RequestParam(value = RequestParamConstants.TREATMENT_ID, required = false) Long treatmentID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading View Treatment");

		Optional.ofNullable(treatmentID).ifPresent(id -> logger.debug("Treatment ID: {}", id));

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