package com.healthcaremngnt.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.enums.Priority;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointments")
public class Appointment {

	private static final Logger logger = LogManager.getLogger(Appointment.class);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "appointment_id")
	private Long appointmentID;

	@Column(name = "appointment_date", nullable = false)
	private LocalDate appointmentDate;

	@Column(name = "appointment_time", nullable = false)
	private LocalTime appointmentTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "appointment_status", nullable = false)
	private AppointmentStatus appointmentStatus;

	@Column(name = "reason_to_visit")
	private String reasonToVisit;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "doctor_id", nullable = false)
	private Doctor doctor;

	@OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
	private Treatment treatment;

	@Column(name = "created_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdDate;

	@Column(name = "updated_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime updatedDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false)
	private Priority priority;

	@Column(name = "needs_reminder", nullable = false)
	private Boolean needsReminder = false;

	@Column(name = "is_followup", nullable = false)
	private Boolean isFollowup = false;

	// Enhanced follow-up support
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_appointment_id")
	private Appointment parentAppointment;

	@OneToMany(mappedBy = "parentAppointment", fetch = FetchType.LAZY)
	private List<Appointment> followUpAppointments;

	// Soft delete support
	@Column(name = "is_deleted", nullable = false)
	private Boolean deleted = false;

	@Column(name = "deleted_date")
	private LocalDateTime deletedDate;

	// Cancellation reason for better tracking
	@Column(name = "cancellation_reason")
	private String cancellationReason;

	@Column(name = "calendar_event_id")
	private String calendarEventId;

	@PrePersist
	protected void onCreate() {
		createdDate = LocalDateTime.now();
		updatedDate = LocalDateTime.now();
		validateBusinessRules();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedDate = LocalDateTime.now();
		validateBusinessRules();

		// Handle soft delete
		if (deleted && deletedDate == null) {
			deletedDate = LocalDateTime.now();
		}
	}

	private void validateBusinessRules() {
		// Log warning if treatment exists for non-treatment statuses
		if ((appointmentStatus == AppointmentStatus.NOSHOW || appointmentStatus == AppointmentStatus.CANCELED)
				&& treatment != null) {
			logger.warn("Treatment exists for appointment {} with status: {}", appointmentID, appointmentStatus);
		}

		// Validate follow-up logic
		if (isFollowup && parentAppointment == null) {
			logger.warn("Appointment {} marked as follow-up but has no parent appointment", appointmentID);
		}
	}

	// Soft delete method
	public void softDelete(String reason) {
		this.deleted = true;
		this.deletedDate = LocalDateTime.now();
		this.cancellationReason = reason;
		this.appointmentStatus = AppointmentStatus.CANCELED;
	}

	/**
	 * @return the appointmentID
	 */
	public Long getAppointmentID() {
		return appointmentID;
	}

	/**
	 * @param appointmentID the appointmentID to set
	 */
	public void setAppointmentID(Long appointmentID) {
		this.appointmentID = appointmentID;
	}

	/**
	 * @return the appointmentDate
	 */
	public LocalDate getAppointmentDate() {
		return appointmentDate;
	}

	/**
	 * @param appointmentDate the appointmentDate to set
	 */
	public void setAppointmentDate(LocalDate appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	/**
	 * @return the appointmentTime
	 */
	public LocalTime getAppointmentTime() {
		return appointmentTime;
	}

	/**
	 * @param appointmentTime the appointmentTime to set
	 */
	public void setAppointmentTime(LocalTime appointmentTime) {
		this.appointmentTime = appointmentTime;
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

	/**
	 * @return the reasonToVisit
	 */
	public String getReasonToVisit() {
		return reasonToVisit;
	}

	/**
	 * @param reasonToVisit the reasonToVisit to set
	 */
	public void setReasonToVisit(String reasonToVisit) {
		this.reasonToVisit = reasonToVisit;
	}

	/**
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	/**
	 * @return the doctor
	 */
	public Doctor getDoctor() {
		return doctor;
	}

	/**
	 * @param doctor the doctor to set
	 */
	public void setDoctor(Doctor doctor) {
		this.doctor = doctor;
	}

	/**
	 * @return the treatment
	 */
	public Treatment getTreatment() {
		return treatment;
	}

	/**
	 * @param treatment the treatment to set
	 */
	public void setTreatment(Treatment treatment) {
		this.treatment = treatment;
	}

	/**
	 * @return the createdDate
	 */
	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the updatedDate
	 */
	public LocalDateTime getUpdatedDate() {
		return updatedDate;
	}

	/**
	 * @param updatedDate the updatedDate to set
	 */
	public void setUpdatedDate(LocalDateTime updatedDate) {
		this.updatedDate = updatedDate;
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
	 * @return the isFollowup
	 */
	public Boolean getIsFollowup() {
		return isFollowup;
	}

	/**
	 * @param isFollowup the isFollowup to set
	 */
	public void setIsFollowup(Boolean isFollowup) {
		this.isFollowup = isFollowup;
	}

	/**
	 * @return the needsReminder
	 */
	public Boolean getNeedsReminder() {
		return needsReminder;
	}

	/**
	 * @param needsReminder the needsReminder to set
	 */
	public void setNeedsReminder(Boolean needsReminder) {
		this.needsReminder = needsReminder;
	}

	/**
	 * @return the parentAppointment
	 */
	public Appointment getParentAppointment() {
		return parentAppointment;
	}

	/**
	 * @param parentAppointment the parentAppointment to set
	 */
	public void setParentAppointment(Appointment parentAppointment) {
		this.parentAppointment = parentAppointment;
	}

	/**
	 * @return the followUpAppointments
	 */
	public List<Appointment> getFollowUpAppointments() {
		return followUpAppointments;
	}

	/**
	 * @param followUpAppointments the followUpAppointments to set
	 */
	public void setFollowUpAppointments(List<Appointment> followUpAppointments) {
		this.followUpAppointments = followUpAppointments;
	}

	/**
	 * @return the deleted
	 */
	public Boolean getDeleted() {
		return deleted;
	}

	/**
	 * @param deleted the deleted to set
	 */
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * @return the deletedDate
	 */
	public LocalDateTime getDeletedDate() {
		return deletedDate;
	}

	/**
	 * @param deletedDate the deletedDate to set
	 */
	public void setDeletedDate(LocalDateTime deletedDate) {
		this.deletedDate = deletedDate;
	}

	/**
	 * @return the cancellationReason
	 */
	public String getCancellationReason() {
		return cancellationReason;
	}

	/**
	 * @param cancellationReason the cancellationReason to set
	 */
	public void setCancellationReason(String cancellationReason) {
		this.cancellationReason = cancellationReason;
	}

	/**
	 * @return the calendarEventId
	 */
	public String getCalendarEventId() {
		return calendarEventId;
	}

	/**
	 * @param calendarEventId the calendarEventId to set
	 */
	public void setCalendarEventId(String calendarEventId) {
		this.calendarEventId = calendarEventId;
	}

	@Override
	public String toString() {
		return "Appointment [appointmentID=" + appointmentID + ", appointmentDate=" + appointmentDate
				+ ", appointmentTime=" + appointmentTime + ", appointmentStatus=" + appointmentStatus
				+ ", reasonToVisit=" + reasonToVisit + ", doctorID=" + (doctor != null ? doctor.getDoctorID() : null)
				+ ", patientID=" + (patient != null ? patient.getPatientID() : null) + ", treatmentID="
				+ (treatment != null ? treatment.getTreatmentID() : null) + ", createdDate=" + createdDate
				+ ", updatedDate=" + updatedDate + ", priority=" + priority + ", needsReminder=" + needsReminder
				+ ", isFollowup=" + isFollowup + ", parentAppointment=" + parentAppointment + ", followUpAppointments="
				+ followUpAppointments + ", deleted=" + deleted + ", deletedDate=" + deletedDate
				+ ", cancellationReason=" + cancellationReason + ", calendarEventId=" + calendarEventId + "]";
	}

}