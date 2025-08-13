package com.healthcaremngnt.service;

import java.util.List;

import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.exceptions.AppointmentNotFoundException;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.AppointmentRequest;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.Treatment;

public interface AppointmentService {

	Appointment bookAppointment(AppointmentRequest appointmentRequest, Long treatmentID, Long parentAppointmentID, boolean isFollowup);

	Appointment getAppointmentDetails(Long appointmentID) throws AppointmentNotFoundException;

	Appointment updateAppointmentDetails(Appointment appointment) throws AppointmentNotFoundException;

	List<Appointment> getTodaysAppointments(Doctor doctor);

	List<Appointment> getUpcomingAppointments(Patient patient);

	void updateAppointmentStatusAndTreatment(Long appointmentID, AppointmentStatus appointmentStatus,
			Treatment treatment) throws AppointmentNotFoundException;

	List<Appointment> getRecentVisits(Long patientID);

	void sendAppointmentEmail(Appointment appointment);

}