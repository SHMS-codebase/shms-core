package com.healthcaremngnt.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.exceptions.PatientNotFoundException;
import com.healthcaremngnt.model.MedicalHistory;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.repository.MedicalHistoryRepository;
import com.healthcaremngnt.repository.PatientRepository;
import com.healthcaremngnt.repository.UserRepository;
import com.healthcaremngnt.service.PatientService;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

	private static final Logger logger = LogManager.getLogger(PatientServiceImpl.class);

	private final PatientRepository patientRepository;
	private final MedicalHistoryRepository medicalHistoryRepository;
	private final UserRepository userRepository;

	public PatientServiceImpl(PatientRepository patientRepository, MedicalHistoryRepository medicalHistoryRepository,
			UserRepository userRepository) {
		this.patientRepository = patientRepository;
		this.medicalHistoryRepository = medicalHistoryRepository;
		this.userRepository = userRepository;
	}

	@Override
	public Patient register(Patient patient) {
		logger.info("Registering patient: {}", patient);

		validatePatient(patient);

		Patient savedPatient = patientRepository.save(patient);
		logger.info("Patient successfully registered with ID: {}", savedPatient.getPatientID());

		return savedPatient;
	}

	private void validatePatient(Patient patient) {
		if (patient == null) {
			throw new IllegalArgumentException("Patient object cannot be null.");
		}
	}

	@Override
	public Patient getPatientInfoCard(String userName) throws PatientNotFoundException {
		logger.info("Retrieving patient info card for username: {}", userName);

		User user = userRepository.findByUserName(userName)
				.orElseThrow(() -> new PatientNotFoundException("User not found with username: " + userName));

		return patientRepository.findByUser(user).orElseThrow(
				() -> new PatientNotFoundException("Patient information not found for username: " + userName));
	}

	@Override
	public List<MedicalHistory> findMedicalHistory(Long patientID) throws PatientNotFoundException {
		logger.info("Finding medical history for Patient ID: {}", patientID);

		Patient patient = patientRepository.findById(patientID)
				.orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientID));

		List<MedicalHistory> medicalHistory = medicalHistoryRepository.findByPatient(patient);
		logger.info("Found {} medical history records for Patient ID: {}", medicalHistory.size(), patientID);

		return medicalHistory;
	}

	@Override
	public void updateMedicalHistory(List<MedicalHistory> medicalHistoryList) {
		logger.info("Updating medical history records.");

		if (medicalHistoryList == null || medicalHistoryList.isEmpty()) {
			throw new IllegalArgumentException("Medical history list cannot be null or empty.");
		}

		medicalHistoryRepository.saveAll(medicalHistoryList);
		logger.info("Medical history updated successfully.");
	}

	@Override
	public MedicalHistory addMedicalHistory(String newMedicalHistory, Long patientID) throws PatientNotFoundException {
		logger.info("Adding medical history for Patient ID: {}", patientID);

		Patient patient = patientRepository.findById(patientID)
				.orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientID));

		MedicalHistory medicalHistory = new MedicalHistory();
		medicalHistory.setPatient(patient);
		medicalHistory.setMedicalHistory(newMedicalHistory);

		MedicalHistory savedHistory = medicalHistoryRepository.save(medicalHistory);
		logger.info("Medical history successfully added for Patient ID: {}", savedHistory.getPatient().getPatientID());

		return savedHistory;
	}

	@Override
	public List<Patient> getAllPatients() {
	    logger.info("Fetching all patients from database.");
	    
	    return patientRepository.findAll();
	}

	@Override
	public Patient getPatientDetails(Long patientID) throws PatientNotFoundException {
	    logger.info("Fetching patient details for ID: {}", patientID);

	    return patientRepository.findById(patientID)
	            .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientID));
	}

}