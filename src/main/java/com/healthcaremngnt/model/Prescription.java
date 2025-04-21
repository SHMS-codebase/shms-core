package com.healthcaremngnt.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.healthcaremngnt.enums.PrescriptionStatus;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "prescriptions")
public class Prescription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "prescription_id")
	private Long prescriptionID;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "treatment_id", nullable = false)
	private Treatment treatment;

	@Column(name = "prescription_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
	private LocalDate prescriptionDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "prescription_status", nullable = false)
	private PrescriptionStatus prescriptionStatus;

	@Column(name = "general_instructions", columnDefinition = "TEXT")
	private String generalInstructions;

	@OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<PrescriptionDetail> prescriptionDetails; // List of prescription details

	@Column(name = "created_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "updated_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime updatedDate;

	@PrePersist
	protected void onCreate() {
		createdDate = LocalDateTime.now();
		updatedDate = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedDate = LocalDateTime.now();
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
	 * @return the prescriptionStatus
	 */
	public PrescriptionStatus getPrescriptionStatus() {
		return prescriptionStatus;
	}

	/**
	 * @param prescriptionStatus the prescriptionStatus to set
	 */
	public void setPrescriptionStatus(PrescriptionStatus prescriptionStatus) {
		this.prescriptionStatus = prescriptionStatus;
	}

	/**
	 * @return the generalInstructions
	 */
	public String getGeneralInstructions() {
		return generalInstructions;
	}

	/**
	 * @param generalInstructions the generalInstructions to set
	 */
	public void setGeneralInstructions(String generalInstructions) {
		this.generalInstructions = generalInstructions;
	}

	/**
	 * @return the prescriptionDetails
	 */
	public List<PrescriptionDetail> getPrescriptionDetails() {
		return prescriptionDetails;
	}

	/**
	 * @param prescriptionDetails the prescriptionDetails to set
	 */
	public void setPrescriptionDetails(List<PrescriptionDetail> prescriptionDetails) {
		this.prescriptionDetails = prescriptionDetails;
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

	@Override
	public String toString() {
		return "Prescription [prescriptionID=" + prescriptionID + ", treatment=" + treatment + ", prescriptionDate="
				+ prescriptionDate + ", prescriptionStatus=" + prescriptionStatus + ", generalInstructions="
				+ generalInstructions + ", prescriptionDetails=" + prescriptionDetails + ", createdDate=" + createdDate
				+ ", updatedDate=" + updatedDate + "]";
	}

}