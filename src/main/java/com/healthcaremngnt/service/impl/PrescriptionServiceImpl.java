package com.healthcaremngnt.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

		logger.info("PrescriptionServiceImpl::: createPrescription()");

		logger.debug("prescription: {}", prescription);
		logger.debug("prescriptionDetails: {}", prescriptionDetails);

		if (!prescriptionDetails.isEmpty()) {
			Prescription savedPrescription = prescriptionRepository.save(prescription);
			logger.debug("savedPrescription: {}", savedPrescription);
			logger.debug("prescriptionDetails: {}", prescriptionDetails);

			List<PrescriptionDetail> savedPrescriptionDetails = prescriptionDetailService.createPrescriptionDetails(savedPrescription, prescriptionDetails);
			
			logger.debug("savedPrescriptionDetails: {}", savedPrescriptionDetails);
			if(!savedPrescriptionDetails.isEmpty())
				prescription.setPrescriptionDetails(savedPrescriptionDetails);
			
			return savedPrescription;
		}
		return null;
	}

	@Override
	public List<ActivePrescription> getActivePrescriptions(Doctor doctor) {

		logger.info("PrescriptionServiceImpl::: getActivePrescriptions(doctor)");

		List<ActivePrescriptionProjection> projections = activePrescriptionRepository
				.findActivePrescriptionsByDoctor(doctor.getDoctorID());
		List<ActivePrescription> activePrescriptions = new ArrayList<>();
		for (ActivePrescriptionProjection projection : projections) {
			activePrescriptions.add(new ActivePrescription(projection.getPrescriptionID(), projection.getPatientName(),
					projection.getDiagnosis(), projection.getPrescriptionDetailCount(),
					projection.getPrescriptionDate(), projection.getTreatmentStatus(),
					projection.getFollowupNeeded()));
		}
		return activePrescriptions;
	}

	@Override
	public Optional<Prescription> getPrescriptionDetails(Long prescriptionID) {
		
		logger.info("PrescriptionServiceImpl::: getPrescriptionDetails()");

		return prescriptionRepository.findById(prescriptionID);
	}

	@Override
	public void updatePrescriptionDetails(Prescription prescription) throws PrescriptionNotFoundException {
		
		logger.info("PrescriptionServiceImpl::: updatePrescriptionDetails()");

		try {
			if (!prescriptionRepository.existsById(prescription.getPrescriptionID())) {
				throw new PrescriptionNotFoundException(
						"Prescription not found with ID: " + prescription.getPrescriptionID());
			}
			prescriptionRepository.save(prescription);
			if(!prescription.getPrescriptionDetails().isEmpty())
				prescriptionDetailService.updatePrescriptionDetails(prescription);
		} catch (PrescriptionNotFoundException e) {
			logger.error("Prescription not found: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error updating Prescription details: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to update Prescription details", e);
		}
	}

	@Override
	public List<ActivePrescription> getActivePrescriptions(Patient patient) {

		logger.info("PrescriptionServiceImpl::: getActivePrescriptions(patient)");

		List<ActivePrescriptionProjection> projections = activePrescriptionRepository
				.findActivePrescriptionsByPatient(patient.getPatientID());
		List<ActivePrescription> activePrescriptions = new ArrayList<>();
		for (ActivePrescriptionProjection projection : projections) {
			activePrescriptions.add(new ActivePrescription(projection.getPrescriptionID(), projection.getPatientName(),
					projection.getDiagnosis(), projection.getPrescriptionDetailCount(),
					projection.getPrescriptionDate(), projection.getTreatmentStatus(),
					projection.getFollowupNeeded()));
		}
		return activePrescriptions;
	}

	@Override
	public Optional<Prescription> getPrescriptionDetailsByTreatment(Long treatmentID) {
		
		logger.info("PrescriptionServiceImpl::: getPrescriptionDetailsByTreatment()");

		return prescriptionRepository.findByTreatment_TreatmentID(treatmentID);
	}

}