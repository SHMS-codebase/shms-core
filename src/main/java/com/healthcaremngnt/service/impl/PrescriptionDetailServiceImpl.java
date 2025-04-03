package com.healthcaremngnt.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.PrescriptionDetail;
import com.healthcaremngnt.repository.PrescriptionDetailRepository;
import com.healthcaremngnt.service.PrescriptionDetailService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class PrescriptionDetailServiceImpl implements PrescriptionDetailService {

	private static final Logger logger = LogManager.getLogger(PrescriptionDetailServiceImpl.class);

	private final PrescriptionDetailRepository prescriptionDetailRepository;

	public PrescriptionDetailServiceImpl(PrescriptionDetailRepository prescriptionDetailRepository) {
		this.prescriptionDetailRepository = prescriptionDetailRepository;
	}

	@Override
	public List<PrescriptionDetail> createPrescriptionDetails(Prescription prescription,
			List<PrescriptionDetail> prescriptionDetails) {
		logger.info("Creating prescription details for Prescription ID: {}", prescription.getPrescriptionID());

		prescriptionDetails.forEach(detail -> detail.setPrescription(prescription));

		List<PrescriptionDetail> savedDetails = prescriptionDetailRepository.saveAll(prescriptionDetails);
		logger.info("Successfully saved {} prescription details.", savedDetails.size());

		return savedDetails;
	}

	@Override
	public List<PrescriptionDetail> getActivePrescriptions(Doctor doctor) {
		logger.info("Fetching active prescriptions for Doctor ID: {}", doctor.getDoctorID());

		if (doctor.getDoctorID() == null) {
			throw new IllegalArgumentException("Doctor ID cannot be null.");
		}

		return prescriptionDetailRepository.findActivePrescriptionsByDoctorID(doctor.getDoctorID());
	}

	@Override
	public List<PrescriptionDetail> getPrescriptionDetails(Long prescriptionID) {
		logger.info("Fetching prescription details for Prescription ID: {}", prescriptionID);

		if (prescriptionID == null || prescriptionID <= 0) {
			throw new IllegalArgumentException("Invalid prescription ID.");
		}

		return prescriptionDetailRepository.findByPrescription_PrescriptionID(prescriptionID);
	}

	@Override
	public void updatePrescriptionDetails(Prescription prescription) {
		logger.info("Updating prescription details for Prescription ID: {}", prescription.getPrescriptionID());

		if (prescription == null || prescription.getPrescriptionDetails() == null) {
			throw new IllegalArgumentException("Invalid prescription details.");
		}

		List<PrescriptionDetail> existingDetails = prescriptionDetailRepository.findByPrescription(prescription);
		Set<Long> existingIDs = existingDetails.stream().map(PrescriptionDetail::getPrescriptionDetailID)
				.collect(Collectors.toSet());

		Set<Long> updatedIDs = prescription.getPrescriptionDetails().stream()
				.map(detail -> processPrescriptionDetail(detail, prescription)).collect(Collectors.toSet());

		deleteUnusedPrescriptionDetails(existingIDs, updatedIDs);
	}

	private Long processPrescriptionDetail(PrescriptionDetail detail, Prescription prescription) {
		if (detail.getPrescriptionDetailID() != null && detail.getPrescriptionDetailID() > 0) {
			PrescriptionDetail existingDetail = prescriptionDetailRepository.findById(detail.getPrescriptionDetailID())
					.orElseThrow(() -> new EntityNotFoundException("Prescription detail not found"));

			updateExistingPrescriptionDetail(existingDetail, detail);
			prescriptionDetailRepository.save(existingDetail);
			return existingDetail.getPrescriptionDetailID();
		} else {
			detail.setPrescription(prescription);
			detail.setCreatedDate(LocalDateTime.now());
			detail.setUpdatedDate(LocalDateTime.now());
			return prescriptionDetailRepository.save(detail).getPrescriptionDetailID();
		}
	}

	private void updateExistingPrescriptionDetail(PrescriptionDetail existingDetail, PrescriptionDetail detail) {
		existingDetail.setMedicine(detail.getMedicine());
		existingDetail.setDosage(detail.getDosage());
		existingDetail.setFrequency(detail.getFrequency());
		existingDetail.setDuration(detail.getDuration());
		existingDetail.setInstructions(detail.getInstructions());
		existingDetail.setUpdatedDate(LocalDateTime.now());
	}

	private void deleteUnusedPrescriptionDetails(Set<Long> existingIDs, Set<Long> updatedIDs) {
		Set<Long> idsToDelete = new HashSet<>(existingIDs);
		idsToDelete.removeAll(updatedIDs);

		if (!idsToDelete.isEmpty()) {
			logger.info("Deleting {} outdated prescription details.", idsToDelete.size());
			prescriptionDetailRepository.deleteAllById(idsToDelete);
		}
	}

}