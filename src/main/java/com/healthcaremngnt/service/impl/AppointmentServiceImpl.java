package com.healthcaremngnt.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.healthcaremngnt.repository.AppointmentRepository;
import com.healthcaremngnt.repository.DoctorScheduleRepository;
import com.healthcaremngnt.service.AppointmentService;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

	private static final Logger logger = LogManager.getLogger(AppointmentServiceImpl.class);

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private DoctorScheduleRepository doctorScheduleRepository;

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
	public void updateAppointmentDetails(Appointment appointment) throws AppointmentNotFoundException {
		logger.info("Updating appointment details for ID: {}", appointment.getAppointmentID());

		// Ensure the appointment exists; otherwise, throw an exception
		if (!appointmentRepository.existsById(appointment.getAppointmentID())) {
			throw new AppointmentNotFoundException("Appointment not found with ID: " + appointment.getAppointmentID());
		}

		// Save the updated appointment
		appointmentRepository.save(appointment);
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

}