package com.healthcaremngnt.model;

import java.time.LocalDate;

import com.healthcaremngnt.enums.Priority;

public class AppointmentRequest {

	private Long patientID;
	private Long doctorID;
	private LocalDate date;
	private String time;
	private String reason;
	private Priority priority;
	private boolean isFollowup;
	private Long parentAppointmentID;

	// Constructors
	public AppointmentRequest() {
	}

	public AppointmentRequest(Long patientID, Long doctorID, LocalDate date, String time, String reason,
			Priority priority, boolean isFollowup, Long parentAppointmentID) {
		this.patientID = patientID;
		this.doctorID = doctorID;
		this.date = date;
		this.time = time;
		this.reason = reason;
		this.priority = priority;
		this.isFollowup = isFollowup;
		this.parentAppointmentID = parentAppointmentID;
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

	public boolean isFollowup() {
		return isFollowup;
	}

	public void setFollowup(boolean isFollowup) {
		this.isFollowup = isFollowup;
	}

	/**
	 * @return the parentAppointmentID
	 */
	public Long getParentAppointmentID() {
		return parentAppointmentID;
	}

	/**
	 * @param parentAppointmentID the parentAppointmentID to set
	 */
	public void setParentAppointmentID(Long parentAppointmentID) {
		this.parentAppointmentID = parentAppointmentID;
	}

	@Override
	public String toString() {
		return "AppointmentRequest [patientID=" + patientID + ", doctorID=" + doctorID + ", date=" + date + ", time="
				+ time + ", reason=" + reason + ", priority=" + priority + ", isFollowup=" + isFollowup
				+ ", parentAppointmentID=" + parentAppointmentID + "]";
	}

}