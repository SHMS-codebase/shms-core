package com.healthcaremngnt.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.healthcaremngnt.enums.TreatmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "treatments")
public class Treatment {

	private static final Logger logger = LogManager.getLogger(Treatment.class);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "treatment_id")
	private Long treatmentID;

	@Column(name = "diagnosis")
	private String diagnosis;

	@Lob
	@Column(name = "treatment_details")
	private String treatmentDetails;

	@Column(name = "additional_notes", columnDefinition = "TEXT")
	private String notes;

	@Enumerated(EnumType.STRING)
	@Column(name = "treatment_status", nullable = false)
	private TreatmentStatus treatmentStatus;

	@Column(name = "followup_needed")
	private Boolean followUpNeeded;

	@Column(name = "invoice_generated")
	private Boolean invoiceGenerated;

	@Column(name = "treatment_date")
	private LocalDate treatmentDate;

	@OneToMany(mappedBy = "treatment", fetch = FetchType.EAGER)
	private List<Appointment> appointments;

	@Column(name = "doctor_id") // Redundant, but useful
	private Long doctorID; // No direct relationship, data from appointment

	@Column(name = "patient_id") // Redundant, but useful
	private Long patientID; // No direct relationship, data from appointment

	@Column(name = "created_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "updated_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime updatedDate;

	@PrePersist
	protected void onCreate() {
		createdDate = LocalDateTime.now();
		updatedDate = LocalDateTime.now();
		updateDoctorAndPatientIDs(); // Call this only on creation
	}

	@PreUpdate
	protected void onUpdate() {
		updatedDate = LocalDateTime.now(); //
	}

	private void updateDoctorAndPatientIDs() {
		logger.info("Treatment Entity::: updateDoctorAndPatientIDs()");

		if (appointments != null && !appointments.isEmpty()) {
			// Get details from the first appointment (or you could implement a different
			// logic)
			Appointment primaryAppointment = appointments.get(0);

			if (primaryAppointment.getDoctor() != null && primaryAppointment.getPatient() != null) {
				logger.debug("To set doctorID and patientID");
				this.doctorID = primaryAppointment.getDoctor().getDoctorID();
				this.patientID = primaryAppointment.getPatient().getPatientID();
			} else {
				logger.error("Warning: Doctor or Patient is null in updateDoctorAndPatientIDs()");
			}
		} else {
			logger.warn("No appointments associated with this treatment");
		}
	}

	/**
	 * @return the treatmentID
	 */
	public Long getTreatmentID() {
		return treatmentID;
	}

	/**
	 * @param treatmentID the treatmentID to set
	 */
	public void setTreatmentID(Long treatmentID) {
		this.treatmentID = treatmentID;
	}

	/**
	 * @return the diagnosis
	 */
	public String getDiagnosis() {
		return diagnosis;
	}

	/**
	 * @param diagnosis the diagnosis to set
	 */
	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}

	/**
	 * @return the treatmentDetails
	 */
	public String getTreatmentDetails() {
		return treatmentDetails;
	}

	/**
	 * @param treatmentDetails the treatmentDetails to set
	 */
	public void setTreatmentDetails(String treatmentDetails) {
		this.treatmentDetails = treatmentDetails;
	}

	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * @return the treatmentStatus
	 */
	public TreatmentStatus getTreatmentStatus() {
		return treatmentStatus;
	}

	/**
	 * @param treatmentStatus the treatmentStatus to set
	 */
	public void setTreatmentStatus(TreatmentStatus treatmentStatus) {
		this.treatmentStatus = treatmentStatus;
	}

	/**
	 * @return the followUpNeeded
	 */
	public Boolean getFollowUpNeeded() {
		return followUpNeeded;
	}

	/**
	 * @param followUpNeeded the followUpNeeded to set
	 */
	public void setFollowUpNeeded(Boolean followUpNeeded) {
		this.followUpNeeded = followUpNeeded;
	}

	/**
	 * @return the invoiceGenerated
	 */
	public Boolean getInvoiceGenerated() {
		return invoiceGenerated;
	}

	/**
	 * @param invoiceGenerated the invoiceGenerated to set
	 */
	public void setInvoiceGenerated(Boolean invoiceGenerated) {
		this.invoiceGenerated = invoiceGenerated;
	}

	/**
	 * @return the treatmentDate
	 */
	public LocalDate getTreatmentDate() {
		return treatmentDate;
	}

	/**
	 * @param treatmentDate the treatmentDate to set
	 */
	public void setTreatmentDate(LocalDate treatmentDate) {
		this.treatmentDate = treatmentDate;
	}

	/**
	 * @return the appointments
	 */
	public List<Appointment> getAppointments() {
		return appointments;
	}

	/**
	 * @param appointments the appointments to set
	 */
	public void setAppointments(List<Appointment> appointments) {
		this.appointments = appointments;
	}

	/**
	 * @return the doctorID
	 */
	public Long getDoctorID() {
		return doctorID;
	}

	/**
	 * @param doctorID the doctorID to set
	 */
	public void setDoctorID(Long doctorID) {
		this.doctorID = doctorID;
	}

	/**
	 * @return the patientID
	 */
	public Long getPatientID() {
		return patientID;
	}

	/**
	 * @param patientID the patientID to set
	 */
	public void setPatientID(Long patientID) {
		this.patientID = patientID;
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
	 * @return the logger
	 */
	public static Logger getLogger() {
		return logger;
	}

	@Override
	public String toString() {
		return "Treatment [treatmentID=" + treatmentID + ", diagnosis=" + diagnosis + ", treatmentDetails="
				+ treatmentDetails + ", notes=" + notes + ", treatmentStatus=" + treatmentStatus + ", followUpNeeded="
				+ followUpNeeded + ", invoiceGenerated=" + invoiceGenerated + ", treatmentDate=" + treatmentDate
				+ ", appointments=" + appointments + ", doctorID=" + doctorID + ", patientID=" + patientID
				+ ", createdDate=" + createdDate + ", updatedDate=" + updatedDate + "]";
	}

}