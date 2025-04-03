package com.healthcaremngnt.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.enums.InvoiceStatus;
import com.healthcaremngnt.enums.ScheduleStatus;
import com.healthcaremngnt.enums.TreatmentStatus;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.DoctorSchedule;
import com.healthcaremngnt.model.Invoice;
import com.healthcaremngnt.model.Role;
import com.healthcaremngnt.model.SearchResult;
import com.healthcaremngnt.model.Treatment;
import com.healthcaremngnt.repository.AppointmentRepository;
import com.healthcaremngnt.repository.DoctorRepository;
import com.healthcaremngnt.repository.DoctorScheduleRepository;
import com.healthcaremngnt.repository.InvoiceRepository;
import com.healthcaremngnt.repository.PatientRepository;
import com.healthcaremngnt.repository.RoleRepository;
import com.healthcaremngnt.repository.TreatmentRepository;
import com.healthcaremngnt.repository.UserRepository;
import com.healthcaremngnt.service.SearchService;

@Service
@Transactional
public class SearchServiceImpl implements SearchService {

	private static final Logger logger = LogManager.getLogger(SearchServiceImpl.class);

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final DoctorRepository doctorRepository;
	private final PatientRepository patientRepository;
	private final DoctorScheduleRepository doctorScheduleRepository;
	private final AppointmentRepository appointmentRepository;
	private final InvoiceRepository invoiceRepository;
	private final TreatmentRepository treatmentRepository;

	public SearchServiceImpl(RoleRepository roleRepository, UserRepository userRepository,
			DoctorRepository doctorRepository, PatientRepository patientRepository,
			DoctorScheduleRepository doctorScheduleRepository, AppointmentRepository appointmentRepository,
			InvoiceRepository invoiceRepository, TreatmentRepository treatmentRepository) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.doctorRepository = doctorRepository;
		this.patientRepository = patientRepository;
		this.doctorScheduleRepository = doctorScheduleRepository;
		this.appointmentRepository = appointmentRepository;
		this.invoiceRepository = invoiceRepository;
		this.treatmentRepository = treatmentRepository;
	}

	@Override
	public List<SearchResult> searchUsers(String roleName, String name, String emailID, String contactNumber,
			String specializationName) {
		logger.info("Executing user search. Role: {}, Name: {}, Email: {}, Contact: {}, Specialization: {}", roleName,
				name, emailID, contactNumber, specializationName);

		List<SearchResult> searchResults = new ArrayList<>();

		if (roleName != null && !roleName.isBlank()) {
			logger.debug("Performing role-based search.");
			Role role = roleRepository.findByRoleNameIgnoreCase(roleName)
					.orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

			searchResults = performRoleBasedSearch(role, name, emailID, contactNumber, specializationName);
		} else {
			logger.debug("Performing general search.");
			performGeneralSearch(name, emailID, contactNumber, specializationName, searchResults);
		}

		logger.info("Search completed. Total results found: {}", searchResults.size());
		return searchResults;
	}

	private List<SearchResult> performRoleBasedSearch(Role role, String name, String emailID, String contactNumber,
			String specializationName) {
		List<SearchResult> results = new ArrayList<>();
		switch (role.getRoleName().toLowerCase()) {
		case "admin":
			userRepository.searchAdminUsers(name, emailID)
					.forEach(user -> results.add(new SearchResult(user.getUserID(), role.getRoleName(),
							user.getUserName(), user.getEmailID(), "", "")));
			break;
		case "doctor":
			doctorRepository.searchDoctors(name, emailID, contactNumber, specializationName)
					.forEach(doc -> results
							.add(new SearchResult(doc.getUser().getUserID(), role.getRoleName(), doc.getDoctorName(),
									doc.getUser().getEmailID(), doc.getContactNumber(), doc.getSpecialization())));
			break;
		case "patient":
			patientRepository.searchPatients(name, emailID, contactNumber)
					.forEach(pat -> results.add(new SearchResult(pat.getUser().getUserID(), role.getRoleName(),
							pat.getPatientName(), pat.getUser().getEmailID(), pat.getContactNumber(), "")));
			break;
		default:
			logger.warn("Unsupported role: {}", role.getRoleName());
			break;
		}
		return results;
	}

	private void performGeneralSearch(String name, String emailID, String contactNumber, String specializationName,
			List<SearchResult> searchResults) {
		logger.debug("Performing general search with criteria.");
		if (name != null && !name.isBlank()) {
			performSearch(name, emailID, contactNumber, specializationName, searchResults);
		} else if (emailID != null && !emailID.isBlank()) {
			performSearch(name, emailID, contactNumber, specializationName, searchResults);
		} else if (contactNumber != null && !contactNumber.isBlank()) {
			performSearch(name, emailID, contactNumber, specializationName, searchResults);
		} else if (specializationName != null && !specializationName.isBlank()) {
			performSearch(name, emailID, contactNumber, specializationName, searchResults);
		} else {
			performSearch(name, emailID, contactNumber, specializationName, searchResults);
		}
	}

	private void performSearch(String name, String email, String contactNumber, String specialization,
			List<SearchResult> searchResults) {

		logger.debug("performSearch()");

		userRepository.searchAdminUsers(name, email).forEach(user -> searchResults
				.add(new SearchResult(user.getUserID(), "Admin", user.getUserName(), user.getEmailID(), "", "")));

		doctorRepository.searchDoctors(name, email, contactNumber, specialization).forEach(
				doc -> searchResults.add(new SearchResult(doc.getUser().getUserID(), "Doctor", doc.getDoctorName(),
						doc.getUser().getEmailID(), doc.getContactNumber(), doc.getSpecialization())));

		patientRepository.searchPatients(name, email, contactNumber)
				.forEach(pat -> searchResults.add(new SearchResult(pat.getUser().getUserID(), "Patient",
						pat.getPatientName(), pat.getUser().getEmailID(), pat.getContactNumber(), "")));
	}

	@Override
	public List<DoctorSchedule> searchSchedules(Long doctorID, LocalDate availableDate, String specialization,
			ScheduleStatus scheduleStatus) {
		logger.info("Searching doctor schedules with criteria: Doctor ID: {}, Date: {}, Specialization: {}, Status: {}",
				doctorID, availableDate, specialization, scheduleStatus);

		validateSearchInputs(doctorID, availableDate);

		return doctorScheduleRepository.findSchedules(doctorID, availableDate, specialization, scheduleStatus);
	}

	@Override
	public List<Appointment> searchAppointments(Long patientID, Long doctorID, LocalDate appointmentDate,
			AppointmentStatus appointmentStatus) {
		logger.info("Searching appointments with criteria: Patient ID: {}, Doctor ID: {}, Date: {}, Status: {}",
				patientID, doctorID, appointmentDate, appointmentStatus);

		validateSearchInputs(patientID, appointmentDate);

		return appointmentRepository.findAppointments(patientID, doctorID, appointmentDate, appointmentStatus);
	}

	@Override
	public List<Invoice> searchInvoices(Long patientID, Long invoiceID, LocalDate invoiceDate,
			InvoiceStatus invoiceStatus) {
		logger.info("Searching invoices with criteria: Patient ID: {}, Invoice ID: {}, Date: {}, Status: {}", patientID,
				invoiceID, invoiceDate, invoiceStatus);

		validateSearchInputs(patientID, invoiceDate);

		return invoiceRepository.findInvoices(patientID, invoiceID, invoiceDate, invoiceStatus);
	}

	@Override
	public List<Treatment> searchTreatments(Long patientID, Long treatmentID, LocalDate treatmentDate,
			TreatmentStatus treatmentStatus) {
		logger.info("Searching treatments with criteria: Patient ID: {}, Treatment ID: {}, Date: {}, Status: {}",
				patientID, treatmentID, treatmentDate, treatmentStatus);

		validateSearchInputs(patientID, treatmentDate);

		return treatmentRepository.findTreatments(patientID, treatmentID, treatmentDate, treatmentStatus);
	}

	private void validateSearchInputs(Long id, LocalDate date) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Invalid ID provided for search.");
		}
		if (date != null && date.isAfter(LocalDate.now().plusYears(1))) {
			throw new IllegalArgumentException("Search date cannot be beyond one year in the future.");
		}
	}

}