package com.healthcaremngnt.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "prescriptiondetails")
public class PrescriptionDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "prescription_detail_id")
	private Long prescriptionDetailID;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "medicine_id", nullable = false)
	private MedicineDetail medicine;

	@Column(name = "dosage", nullable = false, length = 100)
	private String dosage;

	@Column(name = "frequency", nullable = false, length = 100)
	private String frequency;

	@Column(name = "duration", nullable = false, length = 100)
	private String duration;

	@Column(name = "instructions", length = 255)
	private String instructions;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "prescription_id", nullable = false)
	private Prescription prescription;

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
	 * @return the prescriptionDetailID
	 */
	public Long getPrescriptionDetailID() {
		return prescriptionDetailID;
	}

	/**
	 * @param prescriptionDetailID the prescriptionDetailID to set
	 */
	public void setPrescriptionDetailID(Long prescriptionDetailID) {
		this.prescriptionDetailID = prescriptionDetailID;
	}

	/**
	 * @return the medicine
	 */
	public MedicineDetail getMedicine() {
		return medicine;
	}

	/**
	 * @param medicine the medicine to set
	 */
	public void setMedicine(MedicineDetail medicine) {
		this.medicine = medicine;
	}

	/**
	 * @return the dosage
	 */
	public String getDosage() {
		return dosage;
	}

	/**
	 * @param dosage the dosage to set
	 */
	public void setDosage(String dosage) {
		this.dosage = dosage;
	}

	/**
	 * @return the frequency
	 */
	public String getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency the frequency to set
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return the duration
	 */
	public String getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(String duration) {
		this.duration = duration;
	}

	/**
	 * @return the instructions
	 */
	public String getInstructions() {
		return instructions;
	}

	/**
	 * @param instructions the instructions to set
	 */
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * @return the prescription
	 */
	public Prescription getPrescription() {
		return prescription;
	}

	/**
	 * @param prescription the prescription to set
	 */
	public void setPrescription(Prescription prescription) {
		this.prescription = prescription;
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
		return "PrescriptionDetail [prescriptionDetailID=" + prescriptionDetailID + ", medicine=" + medicine
				+ ", dosage=" + dosage + ", frequency=" + frequency + ", duration=" + duration + ", instructions="
				+ instructions + ", prescription=" + prescription.getPrescriptionID() + ", createdDate=" + createdDate
				+ ", updatedDate=" + updatedDate + "]";
	}

}