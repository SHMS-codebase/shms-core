package com.healthcaremngnt.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.exceptions.PrescriptionNotFoundException;
import com.healthcaremngnt.model.ActivePrescription;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.PrescriptionDetail;
import com.healthcaremngnt.projection.ActivePrescriptionProjection;
import com.healthcaremngnt.repository.ActivePrescriptionRepository;
import com.healthcaremngnt.repository.PrescriptionRespository;
import com.healthcaremngnt.service.PrescriptionDetailService;
import com.healthcaremngnt.service.PrescriptionService;

@Service
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

	private static final Logger logger = LogManager.getLogger(PrescriptionServiceImpl.class);

	private final PrescriptionRespository prescriptionRepository;

	private final ActivePrescriptionRepository activePrescriptionRepository;

	private PrescriptionDetailService prescriptionDetailService;

	public PrescriptionServiceImpl(PrescriptionRespository prescriptionRepository,
			ActivePrescriptionRepository activePrescriptionRepository) {
		this.prescriptionRepository = prescriptionRepository;
		this.activePrescriptionRepository = activePrescriptionRepository;
	}

	@Autowired
	public void setPrescriptionDetailService(PrescriptionDetailService prescriptionDetailService) {
		this.prescriptionDetailService = prescriptionDetailService;
	}

	@Override
	public Prescription createPrescription(Prescription prescription, List<PrescriptionDetail> prescriptionDetails) {

		logger.info("Creating prescription with details.");

		validatePrescriptionData(prescription, prescriptionDetails);

		Prescription savedPrescription = prescriptionRepository.save(prescription);
		logger.debug("Prescription saved: {}", savedPrescription);

		List<PrescriptionDetail> savedDetails = prescriptionDetailService.createPrescriptionDetails(savedPrescription,
				prescriptionDetails);

		if (!savedDetails.isEmpty()) {
			savedPrescription.setPrescriptionDetails(savedDetails);
		}

		return savedPrescription;
	}

	private void validatePrescriptionData(Prescription prescription, List<PrescriptionDetail> prescriptionDetails) {
		if (prescription == null || prescriptionDetails == null || prescriptionDetails.isEmpty()) {
			throw new IllegalArgumentException("Prescription or prescription details cannot be null or empty.");
		}
	}

	@Override
	public List<ActivePrescription> getActivePrescriptions(Doctor doctor) {
		logger.info("Fetching active prescriptions for Doctor ID: {}", doctor.getDoctorID());

		if (doctor.getDoctorID() == null) {
			throw new IllegalArgumentException("Doctor ID cannot be null.");
		}

		return convertToActivePrescriptionList(
				activePrescriptionRepository.findActivePrescriptionsByDoctor(doctor.getDoctorID()));
	}

	private List<ActivePrescription> convertToActivePrescriptionList(List<ActivePrescriptionProjection> projections) {
		return projections.stream()
				.map(projection -> new ActivePrescription(projection.getPrescriptionID(), projection.getPatientName(),
						projection.getDiagnosis(), projection.getPrescriptionDetailCount(),
						projection.getPrescriptionDate(), projection.getTreatmentStatus(),
						projection.getFollowupNeeded()))
				.collect(Collectors.toList());
	}

	@Override
	public Prescription getPrescriptionDetails(Long prescriptionID) throws PrescriptionNotFoundException {
		logger.info("Fetching prescription for ID: {}", prescriptionID);

		if (prescriptionID == null || prescriptionID <= 0) {
			throw new IllegalArgumentException("Invalid prescription ID.");
		}

		return prescriptionRepository.findById(prescriptionID).orElseThrow(
				() -> new PrescriptionNotFoundException("Prescription not found with ID: " + prescriptionID));
	}

	@Override
	public void updatePrescriptionDetails(Prescription prescription) throws PrescriptionNotFoundException {
		logger.info("Updating prescription details for ID: {}", prescription.getPrescriptionID());

		validatePrescription(prescription);

		prescriptionRepository.save(prescription);
		if (!prescription.getPrescriptionDetails().isEmpty()) {
			prescriptionDetailService.updatePrescriptionDetails(prescription);
		}
	}

	private void validatePrescription(Prescription prescription) throws PrescriptionNotFoundException {
		if (prescription == null || prescription.getPrescriptionID() == null) {
			throw new IllegalArgumentException("Prescription or ID cannot be null.");
		}

		if (!prescriptionRepository.existsById(prescription.getPrescriptionID())) {
			throw new PrescriptionNotFoundException(
					"Prescription not found with ID: " + prescription.getPrescriptionID());
		}
	}

	@Override
	public List<ActivePrescription> getActivePrescriptions(Patient patient) {
		logger.info("Fetching active prescriptions for Patient ID: {}", patient.getPatientID());

		if (patient.getPatientID() == null) {
			throw new IllegalArgumentException("Patient ID cannot be null.");
		}

		return convertToActivePrescriptionList(
				activePrescriptionRepository.findActivePrescriptionsByPatient(patient.getPatientID()));
	}

	@Override
	public Prescription getPrescriptionDetailsByTreatment(Long treatmentID) throws PrescriptionNotFoundException {
		logger.info("Fetching prescription details for Treatment ID: {}", treatmentID);

		if (treatmentID == null || treatmentID <= 0) {
			throw new IllegalArgumentException("Invalid treatment ID.");
		}

		return prescriptionRepository.findByTreatment_TreatmentID(treatmentID).orElseThrow(
				() -> new PrescriptionNotFoundException("No prescription found for treatment ID: " + treatmentID));
	}

	@Override
	public List<Prescription> getPrescriptionByPatientID(Long patientID) {
		logger.info("Fetching prescription details for Patient ID: {}", patientID);

		if (patientID == null || patientID <= 0) {
			throw new IllegalArgumentException("Invalid Patient ID.");
		}

		return prescriptionRepository.findPrescriptionByPatientID(patientID);
	}

}