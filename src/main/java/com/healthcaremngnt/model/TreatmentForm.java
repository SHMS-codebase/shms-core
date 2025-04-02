package com.healthcaremngnt.model;

import com.healthcaremngnt.enums.TreatmentStatus;

public class TreatmentForm {

	private String diagnosis;
	private String treatmentDetails;
	private String notes;
	private Boolean followUpNeeded;
	private TreatmentStatus treatmentStatus;

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

	@Override
	public String toString() {
		return "TreatmentForm [diagnosis=" + diagnosis + ", treatmentDetails=" + treatmentDetails + ", notes=" + notes
				+ ", followUpNeeded=" + followUpNeeded + ", treatmentStatus=" + treatmentStatus + "]";
	}

}