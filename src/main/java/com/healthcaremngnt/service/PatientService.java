package com.healthcaremngnt.service;

import java.util.List;

import com.healthcaremngnt.exceptions.PatientNotFoundException;
import com.healthcaremngnt.model.MedicalHistory;
import com.healthcaremngnt.model.Patient;

public interface PatientService {

	Patient register(Patient patient);

	Patient getPatientInfoCard(String userName) throws PatientNotFoundException;

	List<MedicalHistory> findMedicalHistory(Long patientID) throws PatientNotFoundException;;

	void updateMedicalHistory(List<MedicalHistory> medicalHistoryList);

	MedicalHistory addMedicalHistory(String newMedicalHistory, Long patientID) throws PatientNotFoundException;;

	List<Patient> getAllPatients();

	Patient getPatientDetails(Long patientID) throws PatientNotFoundException;

}