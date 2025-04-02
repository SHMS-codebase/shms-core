package com.healthcaremngnt.controller;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.exceptions.PatientNotFoundException;
import com.healthcaremngnt.model.ActivePrescription;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.MedicalHistory;
import com.healthcaremngnt.model.MedicalHistoryWrapper;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.PatientService;
import com.healthcaremngnt.service.PrescriptionService;

@Controller
@RequestMapping("/patient")
public class PatientController {

	private static final Logger logger = LogManager.getLogger(PatientController.class);

	private final PatientService patientService;
	private final AppointmentService appointmentService;
	private final PrescriptionService prescriptionService;

	public PatientController(PatientService patientService, AppointmentService appointmentService,
			@Lazy PrescriptionService prescriptionService) {
		this.patientService = patientService;
		this.appointmentService = appointmentService;
		this.prescriptionService = prescriptionService;
	}

	@GetMapping("/patientdashboard")
	public String getPatientDashboard(@SessionAttribute(RequestParamConstants.USER_NAME) String userName, Model model) {
		logger.info("Loading Patient Dashboard!!!");
		Optional<Patient> patientOptional = patientService.getPatientInfoCard(userName);
		Patient patient = new Patient();

		if (patientOptional.isPresent()) {
			patient = patientOptional.get();
			logger.debug("Patient Details: {}", patient);
			model.addAttribute("patient", patient);

			List<Appointment> appointments = appointmentService.getUpcomingAppointments(patient);
			model.addAttribute("appointments", appointments);
			logger.debug("appointments: {}", appointments);

			List<ActivePrescription> activePrescriptions = prescriptionService.getActivePrescriptions(patient);
			model.addAttribute("activeprescriptions", activePrescriptions);
			logger.debug("activePrescriptions: {}", activePrescriptions);

		}
		return "patientdashboard";

	}

	@GetMapping("/viewmedicalhistory")
	public String viewMedicalHistory(@RequestParam(RequestParamConstants.PATIENT_ID) Long patientID,
			@RequestParam(RequestParamConstants.USER_ID) Long userID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) throws PatientNotFoundException {
		logger.info("Loading Medical History of patient");

		try {
			if (patientID != null) {
				logger.debug("Patient ID: {}", patientID);
			}

			MedicalHistoryWrapper medicalHistoryWrapper = new MedicalHistoryWrapper();
			medicalHistoryWrapper.setMedicalHistoryList(patientService.findMedicalHistory(patientID));
			logger.debug("medicalHistoryWrapper: {}", medicalHistoryWrapper);

			model.addAttribute("medicalHistoryWrapper", medicalHistoryWrapper);
			model.addAttribute("patientID", patientID);
			model.addAttribute("userID", userID);

		} catch (PatientNotFoundException e) {
			logger.error("{} for {}: {}", MessageConstants.PATIENT_NOT_FOUND, patientID, e);
			model.addAttribute("errorMessage", MessageConstants.PATIENT_NOT_FOUND);
		} catch (Exception e) {
			logger.error("{} for {}: {}", MessageConstants.MEDICAL_HSTY_ERROR, patientID, e);
			model.addAttribute("errorMessage", MessageConstants.MEDICAL_HSTY_ERROR);
		}

		model.addAttribute("source", source);
		return "viewmedicalhistory";
	}

	@PostMapping("/updatemedicalhistory")
	public String updateMedicalHistory(@ModelAttribute MedicalHistoryWrapper medicalHistoryWrapper,
			@RequestParam(RequestParamConstants.PATIENT_ID) Long patientID,
			@RequestParam(RequestParamConstants.USER_ID) Long userID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Update Medical History");
		List<MedicalHistory> medicalHistory = medicalHistoryWrapper.getMedicalHistoryList();

		if (patientID != null) {
			logger.debug("Patient ID: {}", patientID);
		}

		if (medicalHistory != null && !medicalHistory.isEmpty()) {
			logger.debug("medicalHistory is available");
			medicalHistory.forEach(history -> {
				logger.debug("Medical History ID: {}", history.getMedicalHistoryID());
				logger.debug("Medical History: {}", history.getMedicalHistory());
				logger.debug("Created Date: {}", history.getCreatedDate());
				logger.debug("Updated Date: {}", history.getUpdatedDate());
				logger.debug("Patient: {}", history.getPatient());
			});
		}

		try {
			patientService.updateMedicalHistory(medicalHistory);
			logger.debug("{}", MessageConstants.MEDICAL_HSTY_UPD_SUCCESS);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.MEDICAL_HSTY_UPD_FAILURE, e);
			model.addAttribute("errorMessage", MessageConstants.MEDICAL_HSTY_UPD_FAILURE);
			model.addAttribute("medicalHistoryWrapper", medicalHistoryWrapper);
			return "viewmedicalhistory";
		}

		model.addAttribute("medicalHistoryWrapper", medicalHistoryWrapper);
		model.addAttribute("patientID", patientID);
		model.addAttribute("userID", userID);
		model.addAttribute("message", MessageConstants.MEDICAL_HSTY_UPD_SUCCESS);
		model.addAttribute("source", source);
		return "viewmedicalhistory";
	}

	@PostMapping("/addmedicalhistory")
	public String addMedicalHistory(@RequestParam(RequestParamConstants.NEW_MEDICAL_HISTORY) String newMedicalHistory,
			@RequestParam(RequestParamConstants.PATIENT_ID) Long patientID,
			@RequestParam(RequestParamConstants.USER_ID) Long userID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) throws PatientNotFoundException {
		logger.info("Add Medical History");

		MedicalHistoryWrapper medicalHistoryWrapper = new MedicalHistoryWrapper();
		medicalHistoryWrapper.setMedicalHistoryList(patientService.findMedicalHistory(patientID));
		List<MedicalHistory> medicalHistory = medicalHistoryWrapper.getMedicalHistoryList();

		if (medicalHistory != null && !medicalHistory.isEmpty()) {
			logger.debug("medicalHistory is available");
			medicalHistory.forEach(history -> {
				logger.debug("Medical History ID: {}", history.getMedicalHistoryID());
				logger.debug("Medical History: {}", history.getMedicalHistory());
				logger.debug("Created Date: {}", history.getCreatedDate());
				logger.debug("Updated Date: {}", history.getUpdatedDate());
				logger.debug("Patient: {}", history.getPatient());
			});
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Available data - newMedicalHistory: {}, patientID: {}",
					newMedicalHistory != null ? newMedicalHistory : "null", patientID != null ? patientID : "null");
		}

		try {
			MedicalHistory addedMedicalHistory = patientService.addMedicalHistory(newMedicalHistory, patientID);
			medicalHistory.add(addedMedicalHistory);
			medicalHistoryWrapper.setMedicalHistoryList(medicalHistory);
			logger.debug("{}", MessageConstants.MEDICAL_HSTY_ADD_SUCCESS);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.MEDICAL_HSTY_ADD_FAILURE, e.getLocalizedMessage());
			model.addAttribute("errorMessage", MessageConstants.MEDICAL_HSTY_ADD_FAILURE);
			model.addAttribute("medicalHistoryWrapper", medicalHistoryWrapper);
			return "viewmedicalhistory";
		}

		model.addAttribute("medicalHistoryWrapper", medicalHistoryWrapper);
		model.addAttribute("patientID", patientID);
		model.addAttribute("userID", userID);
		model.addAttribute("message", MessageConstants.MEDICAL_HSTY_ADD_SUCCESS);
		model.addAttribute("source", source);
		return "viewmedicalhistory";
	}

}