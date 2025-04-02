package com.healthcaremngnt.service.impl;

import java.util.List;
import java.util.Optional;

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

		return patientRepository.save(patient);
	}

	@Override
	public Optional<Patient> getPatientInfoCard(String userName) {

		logger.info("Retrieving patient info card for user: {}", userName);

		Optional<User> user = userRepository.findByUserName(userName);
		return user.flatMap(u -> patientRepository.findByUser(u));
	}

	@Override
	public List<MedicalHistory> findMedicalHistory(Long patientID) throws PatientNotFoundException {

		logger.info("Finding medical history for patient ID: {}", patientID);

		Patient patient = patientRepository.findById(patientID)
				.orElseThrow(() -> new PatientNotFoundException("Patient not found"));

		return medicalHistoryRepository.findByPatient(patient);
	}

	@Override
	public void updateMedicalHistory(List<MedicalHistory> medicalHistoryList) {

		logger.info("Updating medical history");

		medicalHistoryRepository.saveAll(medicalHistoryList);
	}

	@Override
	public MedicalHistory addMedicalHistory(String newMedicalHistory, Long patientID) throws PatientNotFoundException {

		logger.info("Adding medical history for patient ID: {}", patientID);

		Patient patient = patientRepository.findById(patientID)
				.orElseThrow(() -> new PatientNotFoundException("Patient not found")); // Handle Optional

		MedicalHistory medicalHistory = new MedicalHistory();
		medicalHistory.setPatient(patient);
		medicalHistory.setMedicalHistory(newMedicalHistory);

		return medicalHistoryRepository.save(medicalHistory);
	}

	@Override
	public List<Patient> getAllPatients() {

		logger.info("Retrieving all patients from DB");

		return patientRepository.findAll();
	}

	@Override
	public Optional<Patient> getPatientDetails(Long patientID) {

		logger.info("Retrieving patient details for ID: {}", patientID);

		return patientRepository.findById(patientID);
	}

}