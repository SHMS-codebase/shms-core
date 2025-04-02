package com.healthcaremngnt.model;

import java.time.LocalDate;
import java.util.List;

import com.healthcaremngnt.enums.PrescriptionStatus;

public class PrescriptionForm {

	private Long treatmentID;
	private LocalDate prescriptionDate;
	private PrescriptionStatus prescriptionStatus;
	private String generalInstructions;
	private List<PrescriptionDetail> prescriptionDetails;

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

	@Override
	public String toString() {
		return "PrescriptionForm [treatmentID=" + treatmentID + ", prescriptionDate=" + prescriptionDate
				+ ", prescriptionStatus=" + prescriptionStatus + ", generalInstructions=" + generalInstructions
				+ ", prescriptionDetails=" + prescriptionDetails + "]";
	}

}