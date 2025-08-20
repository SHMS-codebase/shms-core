package com.healthcaremngnt.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.exceptions.AppointmentNotFoundException;
import com.healthcaremngnt.exceptions.BookingException;
import com.healthcaremngnt.exceptions.InvalidRequestException;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.AppointmentRequest;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.Treatment;
import com.healthcaremngnt.model.UpcomingAppointment;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.repository.AppointmentRepository;
import com.healthcaremngnt.repository.DoctorRepository;
import com.healthcaremngnt.repository.DoctorScheduleRepository;
import com.healthcaremngnt.repository.PatientRepository;
import com.healthcaremngnt.repository.TreatmentRepository;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.EmailService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

	private static final Logger logger = LogManager.getLogger(AppointmentServiceImpl.class);

	private final AppointmentRepository appointmentRepository;
	private final DoctorRepository doctorRepository;
	private final DoctorScheduleRepository doctorScheduleRepository;
	private final PatientRepository patientRepository;
	private final EmailService emailService;
	private final TreatmentRepository treatmentRepository;

	public AppointmentServiceImpl(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository,
			DoctorScheduleRepository doctorScheduleRepository, PatientRepository patientRepository,
			EmailService emailService, TreatmentRepository treatmentRepository) {
		this.appointmentRepository = appointmentRepository;
		this.doctorRepository = doctorRepository;
		this.doctorScheduleRepository = doctorScheduleRepository;
		this.patientRepository = patientRepository;
		this.emailService = emailService;
		this.treatmentRepository = treatmentRepository;
	}

	@Override
	public Appointment bookAppointment(AppointmentRequest appointmentRequest, Long treatmentID,
			Long parentAppointmentID, boolean isFollowup) {
		logger.info("Booking appointment for Doctor ID: {} on Date: {} at Time: {}", appointmentRequest.getDoctorID(),
				appointmentRequest.getDate(), appointmentRequest.getTime());

		validateTimeSlot(appointmentRequest.getTime());

		LocalTime[] timeRange = parseTimeSlot(appointmentRequest.getTime());
		LocalTime startTime = timeRange[0];
		LocalTime endTime = timeRange[1];

		return doctorScheduleRepository
				.findByDoctor_DoctorIDAndAvailableDateAndTimeSlot(appointmentRequest.getDoctorID(),
						appointmentRequest.getDate(), startTime, endTime)
				.map(schedule -> {
					if (schedule.getAvailableCount() <= 0) {
						throw new BookingException("No available slots for the selected time.");
					}

					schedule.setAvailableCount(schedule.getAvailableCount() - 1);
					doctorScheduleRepository.save(schedule);

					// Creating Appointment from the request
					Appointment appointment = createAppointmentFromRequest(appointmentRequest, treatmentID,
							parentAppointmentID, isFollowup);

					appointment = appointmentRepository.save(appointment);

					// Update treatment followupNeeded field if this is a followup appointment
					if (isFollowup && treatmentID != null) {
						logger.debug("Updating followupNeeded for treatment ID: {}", treatmentID);
						updateTreatmentFollowupStatus(treatmentID);
					} else {
						logger.debug("Not updating followupNeeded");
					}

					return appointment;
				}).orElseThrow(() -> new BookingException("No available schedule found for the given time slot."));
	}

	private void updateTreatmentFollowupStatus(Long treatmentID) {
		try {
			var treatment = treatmentRepository.findById(treatmentID)
					.orElseThrow(() -> new EntityNotFoundException("Treatment not found with ID: " + treatmentID));

			treatment.setFollowUpNeeded(false);

			try {
				treatmentRepository.save(treatment);
				logger.info("Updated followUpNeeded to false for treatment ID: {}", treatmentID);
			} catch (DataAccessException dae) {
				logger.error("Database error while saving treatment ID: {}", treatmentID, dae);
			} catch (Exception e) {
				logger.error("Unexpected error while saving treatment ID: {}", treatmentID, e);
			}

		} catch (EntityNotFoundException enfe) {
			logger.warn("Treatment not found for update. ID: {}", treatmentID);
		} catch (Exception e) {
			logger.error("Unexpected error during follow-up status update for treatment ID: {}", treatmentID, e);
		}
	}

	private Appointment createAppointmentFromRequest(AppointmentRequest request, Long treatmentID,
			Long parentAppointmentID, boolean isFollowup) {

		logger.info(
				"Creating appointment from request: {} with Treatment ID: {}, Parent Appointment ID: {} and isFollowp: {} ",
				request, treatmentID, parentAppointmentID, isFollowup);
		Appointment appointment = new Appointment();

		// Fetch existing entities from database
		var doctor = doctorRepository.findById(request.getDoctorID())
				.orElseThrow(() -> new EntityNotFoundException("Doctor not found with ID: " + request.getDoctorID()));

		var patient = patientRepository.findById(request.getPatientID())
				.orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + request.getPatientID()));

		// Set the fetched entities
		appointment.setDoctor(doctor);
		appointment.setPatient(patient);

		// Set other properties
		appointment.setAppointmentDate(request.getDate());

		// Parse time
		String[] timeSlots = request.getTime().split(" - ");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		LocalTime startTime = LocalTime.parse(timeSlots[0], formatter);
		appointment.setAppointmentTime(startTime);

		appointment.setPriority(request.getPriority());
		appointment.setAppointmentStatus(AppointmentStatus.SCHEDULED);
		appointment.setReasonToVisit(request.getReason());
		appointment.setIsFollowup(isFollowup);

		// Handle parent appointment if exists
		if (request.getParentAppointmentID() != null) {
			var parentAppointment = appointmentRepository.findById(request.getParentAppointmentID())
					.orElseThrow(() -> new EntityNotFoundException(
							"Parent appointment not found with ID: " + request.getParentAppointmentID()));
			appointment.setParentAppointment(parentAppointment);
		}

		return appointment;
	}

	private void validateTimeSlot(String timeSlot) {
		if (timeSlot == null || timeSlot.isBlank()) {
			throw new InvalidRequestException("Time slot cannot be empty.");
		}
	}

	private LocalTime[] parseTimeSlot(String timeSlot) {
		String[] timeSlots = timeSlot.split(" - ");
		if (timeSlots.length != 2) {
			throw new InvalidRequestException("Invalid time slot format. Expected 'HH:mm - HH:mm'.");
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		return new LocalTime[] { LocalTime.parse(timeSlots[0], formatter), LocalTime.parse(timeSlots[1], formatter) };
	}

	@Override
	public Appointment getAppointmentDetails(Long appointmentID) throws AppointmentNotFoundException {
		logger.info("Fetching appointment details for ID: {}", appointmentID);

		return appointmentRepository.findById(appointmentID)
				.orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + appointmentID));
	}

	@Override
	public Appointment updateAppointmentDetails(Appointment appointment) throws AppointmentNotFoundException {
		logger.info("Updating appointment details for ID: {}", appointment.getAppointmentID());

		// Ensure the appointment exists; otherwise, throw an exception
		if (!appointmentRepository.existsById(appointment.getAppointmentID())) {
			throw new AppointmentNotFoundException("Appointment not found with ID: " + appointment.getAppointmentID());
		}

		logger.debug("appointment: {}", appointment);
		// Save the updated appointment
		return appointmentRepository.save(appointment);
	}

	@Override
	public List<Appointment> getTodaysAppointments(Doctor doctor) {
		logger.info("Fetching today's appointments for Doctor ID: {}", doctor.getDoctorID());

		return appointmentRepository.findByDoctorAndAppointmentDateAndAppointmentStatus(doctor, LocalDate.now(),
				AppointmentStatus.SCHEDULED);

	}

	@Override
	public List<Appointment> getUpcomingAppointments(Patient patient) {
		logger.info("Fetching upcoming appointments for Patient ID: {}", patient.getPatientID());

		return appointmentRepository.findByPatientAndAppointmentStatusAndAppointmentDateGreaterThanEqual(patient,
				AppointmentStatus.SCHEDULED, LocalDate.now());
	}

	@Override
	public void updateAppointmentStatusAndTreatment(Long appointmentID, AppointmentStatus appointmentStatus,
			Treatment treatment) throws AppointmentNotFoundException {
		logger.info("Updating appointment status for ID: {} to {}", appointmentID, appointmentStatus);

		if (!appointmentRepository.existsById(appointmentID)) {
			throw new AppointmentNotFoundException("Appointment not found with ID: " + appointmentID);
		}

		appointmentRepository.updateAppointmentStatusAndTreatment(appointmentID, appointmentStatus, treatment);
	}

	@Override
	public List<Appointment> getRecentVisits(Long patientID) {
		logger.info("Fetching Recent Visits for the Patient ID: {}", patientID);

		var patientOptional = patientRepository.findById(patientID);
		List<Appointment> recentVisits = new ArrayList<>();

		var dateLimit = LocalDate.now().minusMonths(3);
		logger.debug("dateLimit: {}", dateLimit);

		if (patientOptional.isPresent()) {
			recentVisits = appointmentRepository.findByPatientAndAppointmentStatusAndAppointmentDateGreaterThanEqual(
					patientOptional.get(), AppointmentStatus.COMPLETED, dateLimit);
		}

		return recentVisits;
	}

	// @Async
	@Override
	public void sendAppointmentEmail(Appointment appointment) {
		if (appointment == null) {
			logger.warn("Appointment is null, email will not be sent.");
			return;
		}

		logger.info("Sending Appointment Email for Appointment ID: {}", appointment.getAppointmentID());
		logger.debug("appointment: {}", appointment);

		// Fetch doctor details safely
		Optional<Doctor> doctorOptional = Optional.ofNullable(appointment.getDoctor())
				.map(doctor -> doctorRepository.findById(doctor.getDoctorID())).orElse(Optional.empty());

		doctorOptional.ifPresent(appointment::setDoctor);

		// Fetch patient details safely
		Optional<Patient> patientOptional = Optional.ofNullable(appointment.getPatient())
				.map(patient -> patientRepository.findById(patient.getPatientID())).orElse(Optional.empty());

		patientOptional.ifPresent(patient -> {
			appointment.setPatient(patient);
			String emailID = Optional.ofNullable(patient.getUser()).map(User::getEmailID).orElse(null);

			if (emailID != null) {
				logger.debug("Sending email to: {}", emailID);
				emailService.sendAppointmentEmail(emailID, appointment);
			} else {
				logger.warn("Email ID not found for patient, email will not be sent.");
			}
		});

		logger.debug("Final appointment state: {}", appointment);
	}

	@Override
	public List<Appointment> getUpcomingAppointments() {
		logger.info("Fetching all appointments for Cancellation.");

		List<Appointment> appointments = appointmentRepository.findByAppointmentStatus(AppointmentStatus.SCHEDULED);

		if (appointments.isEmpty()) {
			logger.warn("No appointments are available for Cancellation.");
		}

		return appointments;
	}

	@Override
	public List<UpcomingAppointment> getUpcomingAppointmentBetween(LocalDate nextWeekDay, LocalDate today) {
		logger.info("Fetching all scheduled appointments for the next 7 days.");

		logger.debug("today: {}, nextWeekDay: {}", today, nextWeekDay);

		List<Appointment> appointments = appointmentRepository
				.findByAppointmentStatusAndAppointmentDateGreaterThanEqualAndAppointmentDateLessThanEqual(
						AppointmentStatus.SCHEDULED, today, nextWeekDay);

		if (appointments.isEmpty()) {
			logger.warn("No appointments are available for Cancellation.");
		}

		return appointments.stream().map(appointment -> {
			Patient patient = appointment.getPatient();
			Doctor doctor = appointment.getDoctor();

			LocalDateTime now = LocalDateTime.now();
			LocalDateTime apptDateTime = LocalDateTime.of(appointment.getAppointmentDate(),
					appointment.getAppointmentTime());
			long hours = Duration.between(now, apptDateTime).toHours();
			String remaining = hours + " hrs remaining";
			boolean eligible = appointment.getAppointmentDate().isAfter(LocalDate.now());

			return new UpcomingAppointment(appointment.getAppointmentID(), patient.getPatientID(),
					patient.getPatientName(), doctor.getDoctorName(), doctor.getSpecialization(),
					appointment.getAppointmentDate(), appointment.getAppointmentTime(),
					appointment.getAppointmentStatus().name(), remaining, eligible);
		}).toList();
		// return appointments;
	}

}