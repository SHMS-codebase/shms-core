package com.healthcaremngnt.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
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
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.repository.AppointmentRepository;
import com.healthcaremngnt.repository.DoctorRepository;
import com.healthcaremngnt.repository.DoctorScheduleRepository;
import com.healthcaremngnt.repository.PatientRepository;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.EmailService;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

	private static final Logger logger = LogManager.getLogger(AppointmentServiceImpl.class);

	private final AppointmentRepository appointmentRepository;
	private final DoctorRepository doctorRepository;
	private final DoctorScheduleRepository doctorScheduleRepository;
	private final PatientRepository patientRepository;
	private final EmailService emailService;

	public AppointmentServiceImpl(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository,
			DoctorScheduleRepository doctorScheduleRepository, PatientRepository patientRepository,
			EmailService emailService) {
		this.appointmentRepository = appointmentRepository;
		this.doctorRepository = doctorRepository;
		this.doctorScheduleRepository = doctorScheduleRepository;
		this.patientRepository = patientRepository;
		this.emailService = emailService;
	}

	@Override
	public Appointment bookAppointment(AppointmentRequest appointmentRequest) {
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

					return appointmentRepository.save(appointmentRequest.toEntity());
				}).orElseThrow(() -> new BookingException("No available schedule found for the given time slot."));
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
	public void updateAppointmentStatus(Long appointmentID, AppointmentStatus appointmentStatus)
			throws AppointmentNotFoundException {
		logger.info("Updating appointment status for ID: {} to {}", appointmentID, appointmentStatus);

		if (!appointmentRepository.existsById(appointmentID)) {
			throw new AppointmentNotFoundException("Appointment not found with ID: " + appointmentID);
		}

		appointmentRepository.updateAppointmentStatus(appointmentID, appointmentStatus);
	}

	@Override
	public List<Appointment> getRecentVisits(Long patientID) {
		logger.info("Fetching Recent Visits for the Patient ID: {}", patientID);

		Optional<Patient> patientOptional = patientRepository.findById(patientID);
		List<Appointment> recentVisits = new ArrayList<>();

		LocalDate dateLimit = LocalDate.now().minusMonths(3);
		logger.debug("dateLimit: {}", dateLimit);

		if (patientOptional.isPresent()) {
			recentVisits = appointmentRepository.findByPatientAndAppointmentStatusAndAppointmentDateGreaterThanEqual(
					patientOptional.get(), AppointmentStatus.COMPLETED, dateLimit);
		}

		return recentVisits;
	}

	@Async
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

}