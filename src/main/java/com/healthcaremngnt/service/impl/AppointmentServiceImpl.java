package com.healthcaremngnt.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.exceptions.AppointmentNotFoundException;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.AppointmentRequest;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.DoctorSchedule;
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
		logger.info("AppointmentServiceImpl::bookAppointment");

		try {
			String timeSlot = appointmentRequest.getTime();
			if (timeSlot == null || timeSlot.isBlank()) {
				throw new IllegalArgumentException("Time slot cannot be empty");
			}

			String[] timeSlots = timeSlot.split(" - ");
			if (timeSlots.length != 2) {
				throw new IllegalArgumentException("Invalid time slot format. Expected 'HH:mm - HH:mm'");
			}

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
			LocalTime startTime = LocalTime.parse(timeSlots[0], formatter);
			LocalTime endTime = LocalTime.parse(timeSlots[1], formatter);

			DoctorSchedule schedule = doctorScheduleRepository
					.findByDoctor_DoctorIDAndAvailableDateAndTimeSlot(appointmentRequest.getDoctorID(),
							appointmentRequest.getDate(), startTime, endTime)
					.orElseThrow(
							() -> new IllegalArgumentException("No available schedule found for the given time slot."));

			if (schedule.getAvailableCount() <= 0) {
				throw new IllegalStateException("No available slots for the selected time.");
			}

			schedule.setAvailableCount(schedule.getAvailableCount() - 1);
			doctorScheduleRepository.save(schedule);

			Appointment appointment = appointmentRequest.toEntity();
			return appointmentRepository.save(appointment);

		} catch (IllegalArgumentException | IllegalStateException e) {
			logger.error("Error booking appointment: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Unexpected error booking appointment: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to book appointment", e);
		}
	}

	@Override
	public Optional<Appointment> getAppointmentDetails(Long appointmentId) throws AppointmentNotFoundException {
		logger.info("AppointmentServiceImpl::getAppointmentDetails");
		try {
			return appointmentRepository.findById(appointmentId);
		} catch (Exception e) {
			logger.error("Error getting appointment details: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to get appointment details", e);
		}
	}

	@Override
	public void updateAppointmentDetails(Appointment appointment) throws AppointmentNotFoundException {
		logger.info("Updating Appointment Details in Service");
		try {
			if (!appointmentRepository.existsById(appointment.getAppointmentID())) {
				throw new AppointmentNotFoundException(
						"Appointment not found with ID: " + appointment.getAppointmentID());
			}
			appointmentRepository.save(appointment);
		} catch (AppointmentNotFoundException e) {
			logger.error("Appointment not found: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error updating appointment details: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to update appointment details", e);
		}
	}

	@Override
	public List<Appointment> getTodaysAppointments(Doctor doctor) {
		logger.info("AppointmentServiceImpl:::getTodaysAppointments");
		AppointmentStatus appointmentStatus = AppointmentStatus.SCHEDULED;
		return appointmentRepository.findByDoctorAndAppointmentDateAndAppointmentStatus(
				doctor, LocalDate.now(), appointmentStatus);
	}

	@Override
	public List<Appointment> getUpcomingAppointments(Patient patient) {
		logger.info("AppointmentServiceImpl:::getUpcomingAppointments");
		AppointmentStatus appointmentStatus = AppointmentStatus.SCHEDULED;
		return appointmentRepository.findByPatientAndAppointmentStatusAndAppointmentDateGreaterThanEqual(
				patient, appointmentStatus, LocalDate.now());
	}

	@Override
	public void updateAppointmentStatus(Long appointmentID, AppointmentStatus appointmentStatus) {
		logger.info("AppointmentServiceImpl:::updateAppointmentStatus");
		appointmentRepository.updateAppointmentStatus(appointmentID, appointmentStatus);
	}

}