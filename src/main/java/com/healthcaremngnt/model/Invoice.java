package com.healthcaremngnt.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.healthcaremngnt.enums.InvoiceStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoices")
public class Invoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "invoice_id")
	private Long invoiceID;

	@Column(name = "invoice_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
	private LocalDateTime invoiceDate;

	@Column(name = "treatment_cost", precision = 10, scale = 2, nullable = false)
	private BigDecimal treatmentCost;

	@Column(name = "prescription_cost", precision = 10, scale = 2)
	private BigDecimal prescriptionCost;

	@Column(name = "total_amount", precision = 10, scale = 2, nullable = false) // For currency values
	private BigDecimal totalAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "invoice_status", nullable = false)
	private InvoiceStatus invoiceStatus;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "treatment_id", unique = true, nullable = false)
	private Treatment treatment;

	@OneToOne(optional = true)
	@JoinColumn(name = "prescription_id", nullable = true)
	private Prescription prescription;

	@Column(name = "created_date", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
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
	 * @return the invoiceID
	 */
	public Long getInvoiceID() {
		return invoiceID;
	}

	/**
	 * @param invoiceID the invoiceID to set
	 */
	public void setInvoiceID(Long invoiceID) {
		this.invoiceID = invoiceID;
	}

	/**
	 * @return the invoiceDate
	 */
	public LocalDateTime getInvoiceDate() {
		return invoiceDate;
	}

	/**
	 * @param invoiceDate the invoiceDate to set
	 */
	public void setInvoiceDate(LocalDateTime invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	/**
	 * @return the treatmentCost
	 */
	public BigDecimal getTreatmentCost() {
		return treatmentCost;
	}

	/**
	 * @param treatmentCost the treatmentCost to set
	 */
	public void setTreatmentCost(BigDecimal treatmentCost) {
		this.treatmentCost = treatmentCost;
	}

	/**
	 * @return the prescriptionCost
	 */
	public BigDecimal getPrescriptionCost() {
		return prescriptionCost;
	}

	/**
	 * @param prescriptionCost the prescriptionCost to set
	 */
	public void setPrescriptionCost(BigDecimal prescriptionCost) {
		this.prescriptionCost = prescriptionCost;
	}

	/**
	 * @return the totalAmount
	 */
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	/**
	 * @param totalAmount the totalAmount to set
	 */
	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	/**
	 * @return the invoiceStatus
	 */
	public InvoiceStatus getInvoiceStatus() {
		return invoiceStatus;
	}

	/**
	 * @param invoiceStatus the invoiceStatus to set
	 */
	public void setInvoiceStatus(InvoiceStatus invoiceStatus) {
		this.invoiceStatus = invoiceStatus;
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
		return "Invoice [invoiceID=" + invoiceID + ", invoiceDate=" + invoiceDate + ", treatmentCost=" + treatmentCost
				+ ", prescriptionCost=" + prescriptionCost + ", totalAmount=" + totalAmount + ", invoiceStatus="
				+ invoiceStatus + ", treatmentID=" + treatment.getTreatmentID() + ", prescriptionID="
				+ prescription.getPrescriptionID() + ", doctorID=" + treatment.getDoctorID() + ", patientID="
				+ treatment.getPatientID() + ", createdDate=" + createdDate + ", updatedDate=" + updatedDate + "]";
	}

}