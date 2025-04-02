package com.healthcaremngnt.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.PrescriptionDetail;
import com.healthcaremngnt.repository.PrescriptionDetailRepository;
import com.healthcaremngnt.service.PrescriptionDetailService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PrescriptionDetailServiceImpl implements PrescriptionDetailService {

	private static final Logger logger = LogManager.getLogger(PrescriptionDetailServiceImpl.class);

	private final PrescriptionDetailRepository prescriptionDetailRepository;

	public PrescriptionDetailServiceImpl(PrescriptionDetailRepository prescriptionDetailRepository) {
		this.prescriptionDetailRepository = prescriptionDetailRepository;
	}

	@Override
	public List<PrescriptionDetail> createPrescriptionDetails(Prescription prescription, List<PrescriptionDetail> prescriptionDetails) {

		logger.info("PrescriptionDetailServiceImpl::: createPrescriptionDetails()");
		
		for (PrescriptionDetail prescriptionDetail : prescriptionDetails) {
			prescriptionDetail.setPrescription(prescription);
		}

		return prescriptionDetailRepository.saveAll(prescriptionDetails);
	}

	@Override
	public List<PrescriptionDetail> getActivePrescriptions(Doctor doctor) {

		logger.info("PrescriptionDetailServiceImpl::: getActivePrescriptions()");

		return prescriptionDetailRepository.findActivePrescriptionsByDoctorID(doctor.getDoctorID());
	}

	@Override
	public List<PrescriptionDetail> getPrescriptionDetails(Long prescriptionID) {

		logger.info("PrescriptionDetailServiceImpl::: getPrescriptionDetails()");

		return prescriptionDetailRepository.findByPrescription_PrescriptionID(prescriptionID);
	}

	@Override
	public void updatePrescriptionDetails(Prescription prescription) {

		logger.info("PrescriptionDetailServiceImpl::: updatePrescriptionDetails()");

		List<PrescriptionDetail> existingDetails = prescriptionDetailRepository.findByPrescription(prescription);

		Set<Long> existingIDs = existingDetails.stream().map(PrescriptionDetail::getPrescriptionDetailID)
				.collect(Collectors.toSet());

		Set<Long> updatedIDs = new HashSet<>();

		for (PrescriptionDetail detail : prescription.getPrescriptionDetails()) {
			if (detail.getPrescriptionDetailID() != null && detail.getPrescriptionDetailID() > 0) {

				updatedIDs.add(detail.getPrescriptionDetailID());

				PrescriptionDetail existingDetail = prescriptionDetailRepository
						.findById(detail.getPrescriptionDetailID())
						.orElseThrow(() -> new EntityNotFoundException("Prescription detail not found"));

				existingDetail.setMedicine(detail.getMedicine());
				existingDetail.setDosage(detail.getDosage());
				existingDetail.setFrequency(detail.getFrequency());
				existingDetail.setDuration(detail.getDuration());
				existingDetail.setInstructions(detail.getInstructions());
				existingDetail.setUpdatedDate(LocalDateTime.now());

				prescriptionDetailRepository.save(existingDetail);
			} else {

				detail.setPrescription(prescription);
				detail.setCreatedDate(LocalDateTime.now());
				detail.setUpdatedDate(LocalDateTime.now());
				PrescriptionDetail savedDetail = prescriptionDetailRepository.save(detail);
				updatedIDs.add(savedDetail.getPrescriptionDetailID());
			}
		}

		Set<Long> IDsToDelete = new HashSet<>(existingIDs);
		IDsToDelete.removeAll(updatedIDs);

		if (!IDsToDelete.isEmpty()) {
			prescriptionDetailRepository.deleteAllById(IDsToDelete);
		}
	}

}