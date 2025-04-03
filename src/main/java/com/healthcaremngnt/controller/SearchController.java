package com.healthcaremngnt.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.enums.InvoiceStatus;
import com.healthcaremngnt.enums.ScheduleStatus;
import com.healthcaremngnt.enums.TreatmentStatus;
import com.healthcaremngnt.exceptions.PatientNotFoundException;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.DoctorSchedule;
import com.healthcaremngnt.model.Invoice;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.SearchResult;
import com.healthcaremngnt.model.Treatment;
import com.healthcaremngnt.service.DoctorService;
import com.healthcaremngnt.service.PatientService;
import com.healthcaremngnt.service.SearchService;
import com.healthcaremngnt.service.TreatmentService;

@Controller
public class SearchController {

	private static final Logger logger = LogManager.getLogger(SearchController.class);

	private final SearchService searchService;
	private final DoctorService doctorService;
	private final PatientService patientService;
	private final TreatmentService treatmentService;

	public SearchController(SearchService searchService, DoctorService doctorService, PatientService patientService,
			TreatmentService treatmentService) {
		this.searchService = searchService;
		this.doctorService = doctorService;
		this.patientService = patientService;
		this.treatmentService = treatmentService;
	}

	@GetMapping("/searchuser")
	public String viewSearchUser(@RequestParam(value = RequestParamConstants.DOCTOR_ID, required = false) Long doctorID,
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source, Model model) {

		logger.info("Search type: {}", doctorID != null ? "Patient Search" : "User Search");

		// Need to move Roles and Specializations to either
		// DB or properties or Enum rather than hard coding in the view
//		List<Specialization> specialization = specializationService.getAllSpecializations();
//		model.addAttribute("specialization", specialization);

		model.addAttribute("doctorID", doctorID);
		model.addAttribute("source", source);

		return "searchuser";
	}

	@PostMapping("/searchuser")
	public String searchUser(@RequestParam(value = RequestParamConstants.ROLE_NAME, required = false) String roleName,
			@RequestParam(value = RequestParamConstants.NAME, required = false) String name,
			@RequestParam(value = RequestParamConstants.EMAIL_ID, required = false) String emailID,
			@RequestParam(value = RequestParamConstants.CONTACT_NUMBER, required = false) String contactNumber,
			@RequestParam(value = RequestParamConstants.SPECIALIZATION_NAME, required = false) String specializationName,
			@RequestParam(value = RequestParamConstants.DOCTOR_ID, required = false) Long doctorID,
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source, Model model) {

		logger.info("User Search");

		if (hasSearchCriteria(roleName, name, emailID, contactNumber, specializationName)) {
			logger.info(
					"Searching users with criteria - Role Name: {}, Name: {}, Email ID: {}, Contact Number: {}, Specialization Name: {}",
					roleName, name, emailID, contactNumber, specializationName);

			try {
				List<SearchResult> searchResults = searchService.searchUsers(roleName, name, emailID, contactNumber,
						specializationName);

				if (searchResults == null || searchResults.isEmpty()) {
					logger.debug("{}", MessageConstants.NO_USER_RESULTS);
					model.addAttribute("searchResults", null);
					model.addAttribute("message", MessageConstants.NO_USER_RESULTS);
				} else {
					logger.debug("Search Results in Controller: {}", searchResults);
					model.addAttribute("searchResults", searchResults);
				}
			} catch (Exception e) {
				logger.error("{}: {}", MessageConstants.SEARCH_ERROR, e);
				model.addAttribute("errorMessage", MessageConstants.SEARCH_ERROR);
			}
		} else {
			logger.debug("Search Criteria Not Available");
			model.addAttribute("searchResults", null);
			model.addAttribute("message", MessageConstants.SEARCH_CRITERIA_EMPTY);
		}

		model.addAttribute("source", source);
		if (doctorID != null)
			model.addAttribute("doctorID", doctorID);

		return "searchuser";
	}

	private boolean hasSearchCriteria(String roleName, String name, String emailID, String contactNumber,
			String specializationName) {
		return StringUtils.hasText(roleName) || StringUtils.hasText(name) || StringUtils.hasText(emailID)
				|| StringUtils.hasText(contactNumber) || StringUtils.hasText(specializationName);
	}

	@GetMapping("/searchschedule")
	public String viewSearchSchedule(
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source, Model model) {
		logger.info("View Search Schedules!!!");

		try {
			List<Doctor> doctors = doctorService.getAllDoctors();
			model.addAttribute("doctors", doctors);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.DOCTOR_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.DOCTOR_LOAD_ERROR);
		}

		model.addAttribute("source", source);
		return "searchschedule";
	}

	@PostMapping("/searchschedule")
	public String searchSchedules(
			@RequestParam(value = RequestParamConstants.DOCTOR_NAME, required = false) Long doctorID,
			@RequestParam(value = RequestParamConstants.AVAILABLE_DATE, required = false) String availableDateStr,
			@RequestParam(value = RequestParamConstants.SPECIALIZATION_NAME, required = false) String specializationName,
			@RequestParam(value = RequestParamConstants.SCHEDULE_STATUS, required = false) ScheduleStatus scheduleStatus,
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source, Model model) {

		logger.info("Searching schedules with criteria - Doctor ID: {}, Date: {}, Specialization: {}, Status: {}",
				doctorID, availableDateStr, specializationName, scheduleStatus);

		LocalDate availableDate = parseDate(availableDateStr);

		try {
			List<DoctorSchedule> schedules = searchService.searchSchedules(doctorID, availableDate, specializationName,
					scheduleStatus);
			model.addAttribute("schedules", schedules);

			if (schedules == null || schedules.isEmpty()) {
				logger.debug("{}", MessageConstants.NO_SCHEDULE_RESULTS);
				model.addAttribute("message", MessageConstants.NO_SCHEDULE_RESULTS);
			}
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.SEARCH_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.SEARCH_ERROR);
		}

		// Pass the doctor list again for the search form
		List<Doctor> doctors = doctorService.getAllDoctors();
		model.addAttribute("doctors", doctors);
		model.addAttribute("source", source);

		return "searchschedule";
	}

	private LocalDate parseDate(String dateStr) {
		if (StringUtils.hasText(dateStr)) {
			try {
				return LocalDate.parse(dateStr);
			} catch (DateTimeParseException e) {
				logger.error("Invalid date format: {}", dateStr, e);
				throw new IllegalArgumentException("Invalid date format: " + dateStr);
			}
		}
		return null;
	}

	@GetMapping("/searchappointment")
	public String viewSearchAppointment(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("View Search Appointments!!!");

		List<Patient> patients = patientService.getAllPatients();
		model.addAttribute("patients", patients);

		List<Doctor> doctors = doctorService.getAllDoctors();
		model.addAttribute("doctors", doctors);

		model.addAttribute("source", source);

		return "searchappointment";
	}

	@PostMapping("/searchappointment")
	public String searchAppointments(
			@RequestParam(value = RequestParamConstants.PATIENT_ID, required = false) Long patientID,
			@RequestParam(value = RequestParamConstants.DOCTOR_ID, required = false) Long doctorID,
			@RequestParam(value = RequestParamConstants.APPOINTMENT_DATE, required = false) String appointmentDateStr,
			@RequestParam(value = RequestParamConstants.APPOINTMENT_STATUS, required = false) AppointmentStatus appointmentStatus,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {

		logger.info(
				"Searching Appointments with criteria - Patient ID: {}, Doctor ID: {}, Appointment Date: {}, Status: {}, source: {}",
				patientID, doctorID, appointmentDateStr, appointmentStatus, source);

		LocalDate appointmentDate = parseDate(appointmentDateStr);

		try {
			List<Appointment> appointments = searchService.searchAppointments(patientID, doctorID, appointmentDate,
					appointmentStatus);
			model.addAttribute("appointments", appointments);

			if (appointments == null || appointments.isEmpty()) {
				logger.debug("{}", MessageConstants.NO_APMNT_RESULTS);
				model.addAttribute("message", MessageConstants.NO_APMNT_RESULTS);
			}
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.SEARCH_ERROR, e);
			model.addAttribute("message", MessageConstants.SEARCH_ERROR);
		}

		// Pass the doctor list again for the search form
		List<Doctor> doctors = doctorService.getAllDoctors();
		model.addAttribute("doctors", doctors);

		List<Patient> patients = patientService.getAllPatients();
		model.addAttribute("patients", patients);

		model.addAttribute("source", source);

		return "searchappointment";
	}

	@GetMapping("/searchinvoices")
	public String viewSearchInvoices(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("View Search Invoices!!!");

		List<Patient> patients = patientService.getAllPatients();
		model.addAttribute("patients", patients);

		model.addAttribute("source", source);

		return "searchinvoices";
	}

	@PostMapping("/searchinvoices")
	public String searchInvoices(
			@RequestParam(value = RequestParamConstants.INVOICE_ID, required = false) Long invoiceID,
			@RequestParam(value = RequestParamConstants.PATIENT_ID, required = false) Long patientID,
			@RequestParam(value = RequestParamConstants.INVOICE_DATE, required = false) String invoiceDateStr,
			@RequestParam(value = RequestParamConstants.INVOICE_STATUS, required = false) InvoiceStatus invoiceStatus,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {

		logger.info(
				"Searching Appointments with criteria - Patient ID: {}, Invoice ID: {}, Invoice Date: {}, Status: {}, source: {}",
				patientID, invoiceID, invoiceDateStr, invoiceStatus, source);

		LocalDate invoiceDate = parseDate(invoiceDateStr);

		try {
			List<Invoice> invoices = searchService.searchInvoices(patientID, invoiceID, invoiceDate, invoiceStatus);
			model.addAttribute("invoices", invoices);
			model.addAttribute("resultsCount", invoices.size());

			if (invoices == null || invoices.isEmpty()) {
				logger.debug("{}", MessageConstants.NO_INVOICES_RESULTS);
				model.addAttribute("message", MessageConstants.NO_INVOICES_RESULTS);
			}
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.SEARCH_ERROR, e);
			model.addAttribute("message", MessageConstants.SEARCH_ERROR);
		}

		List<Patient> patients = patientService.getAllPatients();
		model.addAttribute("patients", patients);

		model.addAttribute("source", source);

		return "searchinvoices";
	}

	@GetMapping("/searchtreatments")
	public String viewSearchTreatments(@RequestParam(RequestParamConstants.DOCTOR_ID) Long doctorID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) throws PatientNotFoundException {
		logger.info("View Search Treatments!!!");

		// Ensure patientIDs list is not null
		List<Long> patientIDs = treatmentService.getTreatmentDetailsByDoctor(doctorID);
		if (patientIDs == null || patientIDs.isEmpty()) {
			model.addAttribute("patients", Collections.emptyList());
			model.addAttribute("source", source);
			return "searchtreatments";
		}

		// Initialize list to avoid NullPointerException
		List<Patient> patients = new ArrayList<>();

		for (Long patientID : patientIDs) {
			Patient patient = patientService.getPatientDetails(patientID);
			patients.add(patient);
		}

		model.addAttribute("patients", patients);
		model.addAttribute("source", source);

		return "searchtreatments";
	}

	@PostMapping("/searchtreatments")
	public String searchTreatments(
			@RequestParam(value = RequestParamConstants.TREATMENT_ID, required = false) Long treatmentID,
			@RequestParam(value = RequestParamConstants.PATIENT_ID, required = false) Long patientID,
			@RequestParam(value = RequestParamConstants.TREATMENT_DATE, required = false) String treatmentDateStr,
			@RequestParam(value = RequestParamConstants.TREATMENT_STATUS, required = false) TreatmentStatus treatmentStatus,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {

		logger.info(
				"Searching Appointments with criteria - Patient ID: {}, Treatment ID: {}, Treatment Date: {}, Status: {}, source: {}",
				patientID, treatmentID, treatmentDateStr, treatmentStatus, source);

		LocalDate treatmentDate = parseDate(treatmentDateStr);

		try {
			List<Treatment> treatments = searchService.searchTreatments(patientID, treatmentID, treatmentDate,
					treatmentStatus);
			model.addAttribute("treatments", treatments);
			model.addAttribute("resultsCount", treatments.size());

			if (treatments == null || treatments.isEmpty()) {
				logger.debug("{}", MessageConstants.NO_TREATMENTS_RESULTS);
				model.addAttribute("message", MessageConstants.NO_TREATMENTS_RESULTS);
			}
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.SEARCH_ERROR, e);
			model.addAttribute("message", MessageConstants.SEARCH_ERROR);
		}

		List<Patient> patients = patientService.getAllPatients();
		model.addAttribute("patients", patients);

		model.addAttribute("source", source);

		return "searchtreatments";
	}

}