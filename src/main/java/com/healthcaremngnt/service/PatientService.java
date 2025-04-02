package com.healthcaremngnt.service;

import java.util.List;
import java.util.Optional;

import com.healthcaremngnt.exceptions.PatientNotFoundException;
import com.healthcaremngnt.model.MedicalHistory;
import com.healthcaremngnt.model.Patient;

public interface PatientService {

	Patient register(Patient patient);

	Optional<Patient> getPatientInfoCard(String userName);

	List<MedicalHistory> findMedicalHistory(Long patientID) throws PatientNotFoundException;;

	void updateMedicalHistory(List<MedicalHistory> medicalHistoryList);

	MedicalHistory addMedicalHistory(String newMedicalHistory, Long patientID) throws PatientNotFoundException;;

	List<Patient> getAllPatients();

	Optional<Patient> getPatientDetails(Long patientID);

}