package com.healthcaremngnt.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

		logger.info("SearchServiceImpl::: searchUsers()");

		List<SearchResult> searchResults = new ArrayList<>();

		if (roleName != null) {

			logger.debug("Role-Based Search");

			Optional<Role> role = roleRepository.findByRoleNameIgnoreCase(roleName);

			role.ifPresent(r -> {
				switch (r.getRoleName().toLowerCase()) {
				case "admin":
					userRepository.searchAdminUsers(name, emailID)
							.forEach(user -> searchResults.add(new SearchResult(user.getUserID(), r.getRoleName(),
									user.getUserName(), user.getEmailID(), "", "")));
					break;
				case "doctor":
					doctorRepository.searchDoctors(name, emailID, contactNumber, specializationName)
							.forEach(doc -> searchResults.add(new SearchResult(doc.getUser().getUserID(),
									r.getRoleName(), doc.getDoctorName(), doc.getUser().getEmailID(),
									doc.getContactNumber(), doc.getSpecialization())));
					break;
				case "patient":
					patientRepository.searchPatients(name, emailID, contactNumber).forEach(
							pat -> searchResults.add(new SearchResult(pat.getUser().getUserID(), r.getRoleName(),
									pat.getPatientName(), pat.getUser().getEmailID(), pat.getContactNumber(), "")));
					break;
				}
			});
		} else {
			// Other search cases

			logger.debug("Other Search Cases");

			if (name != null && !name.isEmpty()) {
				performSearch(name, emailID, contactNumber, specializationName, searchResults);
			} else if (emailID != null && !emailID.isEmpty()) {
				performSearch(name, emailID, contactNumber, specializationName, searchResults);
			} else if (contactNumber != null && !contactNumber.isEmpty()) {
				performSearch(name, emailID, contactNumber, specializationName, searchResults);
			} else if (specializationName != null && !specializationName.isEmpty()) {
				performSearch(name, emailID, contactNumber, specializationName, searchResults);
			} else {
				performSearch(name, emailID, contactNumber, specializationName, searchResults);
			}

			// Old Working Code
//			logger.debug("Other Search Cases");
//			if (StringUtils.hasText(name)) {
//				performNameBasedSearch(name, emailID, contactNumber, specializationName, searchResults);
//			} else if (StringUtils.hasText(emailID)) {
//				performEmailBasedSearch(name, emailID, contactNumber, specializationName, searchResults);
//			} else if (StringUtils.hasText(contactNumber)) {
//				performContactNumberBasedSearch(name, emailID, contactNumber, specializationName, searchResults);
//			} else if (StringUtils.hasText(specializationName)) {
//				performSpecializationBasedSearch(name, emailID, contactNumber, specializationName, searchResults);
//			} else {
//				performNoCriteriaSearch(searchResults);
//			}
		}

		return searchResults;
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

	// Old Working Code
//	private void performNameBasedSearch(String name, String emailID, String contactNumber, String specialization,
//			List<SearchResult> searchResults) {
//		logger.debug("Name-Based Search");
//		userRepository.searchAdminUsers(name, emailID).forEach(user -> searchResults
//				.add(new SearchResult(user.getUserID(), "Admin", user.getUserName(), user.getEmailID(), "", "")));
//
//		doctorRepository.searchDoctors(name, emailID, contactNumber, specialization).forEach(
//				doc -> searchResults.add(new SearchResult(doc.getUser().getUserID(), "Doctor", doc.getDoctorName(),
//						doc.getUser().getEmailID(), doc.getContactNumber(), doc.getSpecialization())));
//
//		patientRepository.searchPatients(name, emailID, contactNumber)
//				.forEach(pat -> searchResults.add(new SearchResult(pat.getUser().getUserID(), "Patient",
//						pat.getPatientName(), pat.getUser().getEmailID(), pat.getContactNumber(), "")));
//	}
//
//	private void performEmailBasedSearch(String name, String emailID, String contactNumber, String specialization,
//			List<SearchResult> searchResults) {
//		logger.debug("EmailID-Based Search");
//		userRepository.searchAdminUsers(name, emailID).forEach(user -> searchResults
//				.add(new SearchResult(user.getUserID(), "Admin", user.getUserName(), user.getEmailID(), "", "")));
//
//		doctorRepository.searchDoctors(name, emailID, contactNumber, specialization).forEach(
//				doc -> searchResults.add(new SearchResult(doc.getUser().getUserID(), "Doctor", doc.getDoctorName(),
//						doc.getUser().getEmailID(), doc.getContactNumber(), doc.getSpecialization())));
//
//		patientRepository.searchPatients(name, emailID, contactNumber)
//				.forEach(pat -> searchResults.add(new SearchResult(pat.getUser().getUserID(), "Patient",
//						pat.getPatientName(), pat.getUser().getEmailID(), pat.getContactNumber(), "")));
//	}
//
//	private void performContactNumberBasedSearch(String name, String emailID, String contactNumber,
//			String specialization, List<SearchResult> searchResults) {
//		logger.debug("ContactNumber-Based Search");
//		doctorRepository.searchDoctors(name, emailID, contactNumber, specialization).forEach(
//				doc -> searchResults.add(new SearchResult(doc.getUser().getUserID(), "Doctor", doc.getDoctorName(),
//						doc.getUser().getEmailID(), doc.getContactNumber(), doc.getSpecialization())));
//
//		patientRepository.searchPatients(name, emailID, contactNumber)
//				.forEach(pat -> searchResults.add(new SearchResult(pat.getUser().getUserID(), "Patient",
//						pat.getPatientName(), pat.getUser().getEmailID(), pat.getContactNumber(), "")));
//	}
//
//	private void performSpecializationBasedSearch(String name, String emailID, String contactNumber,
//			String specialization, List<SearchResult> searchResults) {
//		logger.debug("Specialization-Based Search");
//		doctorRepository.searchDoctors(name, emailID, contactNumber, specialization).forEach(
//				doc -> searchResults.add(new SearchResult(doc.getUser().getUserID(), "Doctor", doc.getDoctorName(),
//						doc.getUser().getEmailID(), doc.getContactNumber(), doc.getSpecialization())));
//	}
//
//	private void performNoCriteriaSearch(List<SearchResult> searchResults) {
//		logger.debug("No Search Criteria - Displaying all records");
//		userRepository.findAll().forEach(user -> {
//			final String[] uName = { user.getUserName() };
//			final String[] contact = { "" };
//			final String[] spec = { "" };
//			String role = roleRepository.findById(user.getRole().getRoleID()).map(Role::getRoleName).orElse("Unknown");
//			if ("Doctor".equalsIgnoreCase(role)) {
//				Optional.ofNullable(doctorRepository.findByUser(user)).ifPresent(doctor -> {
//					uName[0] = doctor.getDoctorName();
//					contact[0] = doctor.getContactNumber();
//					spec[0] = doctor.getSpecialization();
//				});
//			} else if ("Patient".equalsIgnoreCase(role)) {
//				Optional.ofNullable(patientRepository.findByUser(user)).ifPresent(patient -> {
//					uName[0] = patient.getPatientName();
//					contact[0] = patient.getContactNumber();
//				});
//			}
//			searchResults
//					.add(new SearchResult(user.getUserID(), role, uName[0], user.getEmailID(), contact[0], spec[0]));
//		});
//	}

	@Override
	public List<DoctorSchedule> searchSchedules(Long doctorID, LocalDate availableDate, String specialization,
			ScheduleStatus scheduleStatus) {

		logger.info("SearchServiceImpl::: searchSchedules()");

		return doctorScheduleRepository.findSchedules(doctorID, availableDate, specialization, scheduleStatus);
	}

	@Override
	public List<Appointment> searchAppointments(Long patientID, Long doctorID, LocalDate appointmentDate,
			AppointmentStatus appointmentStatus) {

		logger.info("SearchServiceImpl::: searchAppointments()");

		return appointmentRepository.findAppointments(patientID, doctorID, appointmentDate, appointmentStatus);
	}

	@Override
	public List<Invoice> searchInvoices(Long patientID, Long invoiceID, LocalDate invoiceDate,
			InvoiceStatus invoiceStatus) {

		logger.info("SearchServiceImpl::: searchAppointments()");

		return invoiceRepository.findInvoices(patientID, invoiceID, invoiceDate, invoiceStatus);
	}

	@Override
	public List<Treatment> searchTreatments(Long patientID, Long treatmentID, LocalDate treatmentDate,
			TreatmentStatus treatmentStatus) {

		logger.info("SearchServiceImpl::: searchTreatments()");

		return treatmentRepository.findTreatments(patientID, treatmentID, treatmentDate, treatmentStatus);
	}

}