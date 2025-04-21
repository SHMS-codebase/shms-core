package com.healthcaremngnt.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.MedicineDetail;
import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.PrescriptionDetail;
import com.healthcaremngnt.repository.MedicineDetailRepository;
import com.healthcaremngnt.repository.PrescriptionDetailRepository;
import com.healthcaremngnt.service.PrescriptionDetailService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class PrescriptionDetailServiceImpl implements PrescriptionDetailService {

	private static final Logger logger = LogManager.getLogger(PrescriptionDetailServiceImpl.class);

	private final PrescriptionDetailRepository prescriptionDetailRepository;
	private final MedicineDetailRepository medicineDetailRepository;

	public PrescriptionDetailServiceImpl(PrescriptionDetailRepository prescriptionDetailRepository,
			MedicineDetailRepository medicineDetailRepository) {
		this.prescriptionDetailRepository = prescriptionDetailRepository;
		this.medicineDetailRepository = medicineDetailRepository;
	}

	@Override
	public List<PrescriptionDetail> createPrescriptionDetails(Prescription prescription,
			List<PrescriptionDetail> prescriptionDetails) {
		logger.info("Creating prescription details for Prescription ID: {}", prescription.getPrescriptionID());

		prescriptionDetails.forEach(detail -> detail.setPrescription(prescription));

		List<PrescriptionDetail> savedDetails = prescriptionDetailRepository.saveAll(prescriptionDetails);

		for (PrescriptionDetail prescriptionDetail : savedDetails) {

			Optional<MedicineDetail> medicineDetailOptional = Optional.ofNullable(new MedicineDetail());

			if (prescriptionDetail.getMedicine() != null) {
				medicineDetailOptional = medicineDetailRepository
						.findById(prescriptionDetail.getMedicine().getMedicineID());

				if (medicineDetailOptional.isPresent())
					prescriptionDetail.setMedicine(medicineDetailOptional.get());
			}
		}

		logger.info("Successfully saved {} prescription details.", savedDetails.size());
		logger.debug("savedDetails: {}", savedDetails);

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

		Optional.ofNullable(prescription).map(Prescription::getPrescriptionID)
				.ifPresent(id -> logger.debug("prescription ID from object: {}", id));
		Optional.ofNullable(prescription).map(Prescription::getPrescriptionDate)
				.ifPresent(date -> logger.debug("prescription Date from object: {}", date));
		Optional.ofNullable(prescription).map(Prescription::getPrescriptionStatus)
				.ifPresent(status -> logger.debug("prescription Status from object: {}", status));
		Optional.ofNullable(prescription).map(Prescription::getGeneralInstructions)
				.ifPresent(instructions -> logger.debug("General Instructions from object: {}", instructions));

		if (prescription.getTreatment() != null) {
			logger.debug("Treatment is not null");

		} else {
			logger.debug("Treatment is null");
		}

		if (prescription.getPrescriptionDetails() != null) {
			logger.debug("Prescription Details is not null");

			for (PrescriptionDetail prescriptionDetail : prescription.getPrescriptionDetails()) {
				logger.debug("ID: {}", prescriptionDetail.getPrescriptionDetailID());
				logger.debug("Medicine: {}, {}, {}", prescriptionDetail.getMedicine().getMedicineID(),
						prescriptionDetail.getMedicine().getMedicineName(),
						prescriptionDetail.getMedicine().getMedicineType());
				logger.debug("Dosage: {}", prescriptionDetail.getDosage());
				logger.debug("Frequency: {}", prescriptionDetail.getFrequency());
				logger.debug("Duration: {}", prescriptionDetail.getDuration());
				logger.debug("Instructions: {}", prescriptionDetail.getInstructions());

				// Ensure prescription reference is maintained
				prescriptionDetail.setPrescription(prescription);
			}
		} else {
			logger.debug("Prescription Details is null");
		}

		// Fetch existing details to compare with updated ones
		List<PrescriptionDetail> existingDetails = prescriptionDetailRepository.findByPrescription(prescription);
		logger.debug("existingDetails: {}", existingDetails);

		Set<Long> existingIDs = existingDetails.stream().map(PrescriptionDetail::getPrescriptionDetailID)
				.collect(Collectors.toSet());
		logger.debug("existingIDs: {}", existingIDs);

		// Process each prescription detail, ensuring prescription reference is
		// maintained
		Set<Long> updatedIDs = prescription.getPrescriptionDetails().stream().map(detail -> {
			// Ensure the prescription reference is set for each detail
			detail.setPrescription(prescription);
			return processPrescriptionDetail(detail, prescription);
		}).collect(Collectors.toSet());
		logger.debug("updatedIDs: {}", updatedIDs);

		deleteUnusedPrescriptionDetails(existingIDs, updatedIDs);
	}

	private Long processPrescriptionDetail(PrescriptionDetail detail, Prescription prescription) {
		logger.info("Processing Prescription Details");

		logger.debug("Prescription Details: {}, Prescription: {}", detail, prescription);

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