package com.healthcaremngnt.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.enums.Priority;

public class AppointmentRequest {

	private Long patientID;
	private Long doctorID;
	private LocalDate date;
	private String time;
	private String reason;
	private Priority priority;
	private AppointmentStatus appointmentStatus;

	// Constructors
	public AppointmentRequest() {
	}

	public AppointmentRequest(Long patientID, Long doctorID, LocalDate date, String time, String reason,
			Priority priority, AppointmentStatus appointmentStatus) {
		this.patientID = patientID;
		this.doctorID = doctorID;
		this.date = date;
		this.time = time;
		this.reason = reason;
		this.priority = priority;
		this.appointmentStatus = appointmentStatus;
	}

	// Getters and Setters
	public Long getPatientID() {
		return patientID;
	}

	public void setPatientID(Long patientID) {
		this.patientID = patientID;
	}

	public Long getDoctorID() {
		return doctorID;
	}

	public void setDoctorID(Long doctorID) {
		this.doctorID = doctorID;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @return the priority
	 */
	public Priority getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	/**
	 * @return the appointmentStatus
	 */
	public AppointmentStatus getAppointmentStatus() {
		return appointmentStatus;
	}

	/**
	 * @param appointmentStatus the appointmentStatus to set
	 */
	public void setAppointmentStatus(AppointmentStatus appointmentStatus) {
		this.appointmentStatus = appointmentStatus;
	}

	@Override
	public String toString() {
		return "AppointmentRequest [patientID=" + patientID + ", doctorID=" + doctorID + ", date=" + date + ", time="
				+ time + ", reason=" + reason + ", priority=" + priority + ", appointmentStatus=" + appointmentStatus
				+ "]";
	}

	public Appointment toEntity() {
		Appointment appointment = new Appointment();

		// Setting patient and doctor IDs
		Patient patient = new Patient();
		patient.setPatientID(this.patientID);
		appointment.setPatient(patient);

		Doctor doctor = new Doctor();
		doctor.setDoctorID(this.doctorID);
		appointment.setDoctor(doctor);

		appointment.setAppointmentDate(this.date);

		// Splitting the time slot into startTime and endTime
		String[] timeSlots = this.time.split(" - ");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		LocalTime startTime = LocalTime.parse(timeSlots[0], formatter);
//		LocalTime endTime = LocalTime.parse(timeSlots[1], formatter);
		appointment.setAppointmentTime(startTime);
		appointment.setPriority(this.priority);
		appointment.setAppointmentStatus(this.appointmentStatus);
		appointment.setReasonToVisit(this.reason);

		return appointment;
	}

}