package com.healthcaremngnt.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import net.jcip.annotations.Immutable;

@Entity
@Immutable
@org.hibernate.annotations.Subselect("SELECT 1 FROM DUAL WHERE 1=0")
public class ActivePrescription {

	@Id
	private Long prescriptionID;
	private String patientName;
	private String diagnosis;
	private int numberOfMedications;
	private LocalDate prescriptionDate;
	private String treatmentStatus;
	private Boolean followUpNeeded;

	// Default constructor
	public ActivePrescription() {
	}

	// Constructor matching the query parameters
	public ActivePrescription(Long prescriptionID, String patientName, String diagnosis, Long numberOfMedications,
			LocalDate prescriptionDate, String treatmentStatus, Boolean followUpNeeded) {
		this.prescriptionID = prescriptionID;
		this.patientName = patientName;
		this.diagnosis = diagnosis;
		this.numberOfMedications = numberOfMedications != null ? numberOfMedications.intValue() : 0;
		this.prescriptionDate = prescriptionDate;
		this.treatmentStatus = treatmentStatus;
		this.followUpNeeded = followUpNeeded;
	}

	/**
	 * @return the prescriptionID
	 */
	public Long getPrescriptionID() {
		return prescriptionID;
	}

	/**
	 * @param prescriptionID the prescriptionID to set
	 */
	public void setPrescriptionID(Long prescriptionID) {
		this.prescriptionID = prescriptionID;
	}

	/**
	 * @return the patientName
	 */
	public String getPatientName() {
		return patientName;
	}

	/**
	 * @param patientName the patientName to set
	 */
	public void setPatientName(String patientName) {
		this.patientName = patientName;
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
	 * @return the numberOfMedications
	 */
	public int getNumberOfMedications() {
		return numberOfMedications;
	}

	/**
	 * @param numberOfMedications the numberOfMedications to set
	 */
	public void setNumberOfMedications(int numberOfMedications) {
		this.numberOfMedications = numberOfMedications;
	}

	/**
	 * @return the prescriptionDate
	 */
	public LocalDate getPrescriptionDate() {
		return prescriptionDate;
	}

	/**
	 * @param prescriptionDate the prescriptionDate to set
	 */
	public void setPrescriptionDate(LocalDate prescriptionDate) {
		this.prescriptionDate = prescriptionDate;
	}

	/**
	 * @return the treatmentStatus
	 */
	public String getTreatmentStatus() {
		return treatmentStatus;
	}

	/**
	 * @param treatmentStatus the treatmentStatus to set
	 */
	public void setTreatmentStatus(String treatmentStatus) {
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

	@Override
	public String toString() {
		return "ActivePrescription [prescriptionID=" + prescriptionID + ", patientName=" + patientName + ", diagnosis="
				+ diagnosis + ", numberOfMedications=" + numberOfMedications + ", prescriptionDate=" + prescriptionDate
				+ ", treatmentStatus=" + treatmentStatus + ", followUpNeeded=" + followUpNeeded + "]";
	}

}