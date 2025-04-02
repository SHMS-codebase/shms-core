package com.healthcaremngnt.model;

import java.util.List;

public class MedicalHistoryWrapper {
	private List<MedicalHistory> medicalHistoryList;

	public List<MedicalHistory> getMedicalHistoryList() {
		return medicalHistoryList;
	}

	public void setMedicalHistoryList(List<MedicalHistory> medicalHistoryList) {
		this.medicalHistoryList = medicalHistoryList;
	}

	@Override
	public String toString() {
		return "MedicalHistoryWrapper [medicalHistoryList=" + medicalHistoryList + "]";
	}

}