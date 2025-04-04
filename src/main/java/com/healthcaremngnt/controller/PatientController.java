package com.healthcaremngnt.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.MedicalHistory;
import com.healthcaremngnt.model.MedicalHistoryWrapper;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.Treatment;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.DoctorService;
import com.healthcaremngnt.service.PatientService;
import com.healthcaremngnt.service.PrescriptionService;
import com.healthcaremngnt.service.TreatmentService;

@Controller
@RequestMapping("/patient")
public class PatientController {

	private static final Logger logger = LogManager.getLogger(PatientController.class);

	private final PatientService patientService;
	private final AppointmentService appointmentService;
	private final PrescriptionService prescriptionService;
	private final DoctorService doctorService;
	private final TreatmentService treatmentService;

	public PatientController(PatientService patientService, AppointmentService appointmentService,
			@Lazy PrescriptionService prescriptionService, TreatmentService treatmentService,
			DoctorService doctorService) {
		this.patientService = patientService;
		this.appointmentService = appointmentService;
		this.prescriptionService = prescriptionService;
		this.treatmentService = treatmentService;
		this.doctorService = doctorService;
	}

	@GetMapping("/patientdashboard")
	public String getPatientDashboard(@SessionAttribute(RequestParamConstants.USER_NAME) String userName, Model model)
			throws PatientNotFoundException {
		logger.info("Loading Patient Dashboard!!!");

		Patient patient = patientService.getPatientInfoCard(userName);

		logger.debug("Patient Details: {}", patient);
		model.addAttribute("patient", patient);

		List<Appointment> appointments = appointmentService.getUpcomingAppointments(patient);
		model.addAttribute("appointments", appointments);
		logger.debug("appointments: {}", appointments);

		List<ActivePrescription> activePrescriptions = prescriptionService.getActivePrescriptions(patient);
		model.addAttribute("activeprescriptions", activePrescriptions);
		logger.debug("activePrescriptions: {}", activePrescriptions);

		return "patientdashboard";

	}

	@GetMapping("/viewmedicalhistory")
	public String viewMedicalHistory(@RequestParam(RequestParamConstants.PATIENT_ID) Long patientID,
			@RequestParam(value = RequestParamConstants.USER_ID, required = false) Long userID,
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
			@RequestParam(value = RequestParamConstants.USER_ID, required = false) Long userID,
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
			@RequestParam(value = RequestParamConstants.USER_ID, required = false) Long userID,
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

	@GetMapping("/mydoctors")
	public String viewMyDoctors(@RequestParam(RequestParamConstants.PATIENT_ID) Long patientID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading Doctors treating the Patient ID: {}", patientID);

		try {
			List<Long> doctorIDs = treatmentService.getDoctorListByPatient(patientID);
			if (doctorIDs == null || doctorIDs.isEmpty()) {
				model.addAttribute("doctors", Collections.emptyList());
				logger.debug("{}", MessageConstants.NO_DOCTORS_TREATING_PATIENT);
				return "viewalldoctors";
			}

			// Initialize list to avoid NullPointerException
			List<Doctor> doctors = new ArrayList<>();

			for (Long doctorID : doctorIDs) {
				Doctor doctor = doctorService.getDoctorDetails(doctorID);
				doctors.add(doctor);
			}

			model.addAttribute("doctors", doctors);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.VIEW_MY_DOCTORS_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.VIEW_MY_DOCTORS_LOAD_ERROR);
			return source;
		}

		model.addAttribute("patientID", patientID);
		model.addAttribute("source", source);

		return "viewmydoctors";
	}

	@GetMapping("/recentvisits")
	public String viewRecentVisits(@RequestParam(RequestParamConstants.PATIENT_ID) Long patientID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading Recent Visits of the Patient ID: {}", patientID);

		try {

			List<Appointment> recentVisits = appointmentService.getRecentVisits(patientID);
			logger.debug("recentVisits: {}", recentVisits);
			model.addAttribute("recentvisits", recentVisits);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.VIEW_RECENT_VISITS_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.VIEW_RECENT_VISITS_LOAD_ERROR);
			return source;
		}

		model.addAttribute("patientID", patientID);
		model.addAttribute("source", source);

		return "recentvisits";
	}
	
	@GetMapping("/treatmentdetails")
	public String viewTreatmentDetails(@RequestParam(RequestParamConstants.PATIENT_ID) Long patientID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading Treatment Details of the Patient ID: {}", patientID);

		try {

			List<Treatment> treatmentDetails = treatmentService.getTreatmentDetailsByPatient(patientID);
			logger.debug("treatmentDetails: {}", treatmentDetails);
			model.addAttribute("treatmentdetails", treatmentDetails);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.VIEW_TREATMENT_DETAILS_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.VIEW_TREATMENT_DETAILS_LOAD_ERROR);
			return source;
		}

		model.addAttribute("patientID", patientID);
		model.addAttribute("source", source);

		return "treatmentdetails";
	}
	
	@GetMapping("/prescriptiondetails")
	public String viewPrescriptionDetails(@RequestParam(RequestParamConstants.PATIENT_ID) Long patientID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading Prescription Details of the Patient ID: {}", patientID);

		try {

			List<Prescription> prescriptionDetails = prescriptionService.getPrescriptionByPatientID(patientID);
			logger.debug("prescriptionDetails: {}", prescriptionDetails);
			model.addAttribute("prescriptiondetails", prescriptionDetails);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.VIEW_PRESC_DETAILS_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.VIEW_PRESC_DETAILS_LOAD_ERROR);
			return source;
		}

		model.addAttribute("patientID", patientID);
		model.addAttribute("source", source);

		return "prescriptiondetails";
	}

}