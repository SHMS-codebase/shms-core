package com.healthcaremngnt.service;

import java.util.List;
import java.util.Optional;

import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.exceptions.AppointmentNotFoundException;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.AppointmentRequest;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Patient;

public interface AppointmentService {

	Appointment bookAppointment(AppointmentRequest appointmentRequest);

	Optional<Appointment> getAppointmentDetails(Long appointmentID) throws AppointmentNotFoundException;

	void updateAppointmentDetails(Appointment appointment) throws AppointmentNotFoundException;

	List<Appointment> getTodaysAppointments(Doctor doctor);

	List<Appointment> getUpcomingAppointments(Patient patient);

	void updateAppointmentStatus(Long appointmentID, AppointmentStatus appointmentStatus);

//	List<Appointment> getAppointmentsBetweenDates(LocalDate startDate, LocalDate endDate);

//	List<Appointment> searchAppointments(Long patientID, Long doctorID, LocalDate appointmentDate,
//			String appointmentStatus);

//	List<Appointment> getAppointmentsByPatientID(Long patientID);

}