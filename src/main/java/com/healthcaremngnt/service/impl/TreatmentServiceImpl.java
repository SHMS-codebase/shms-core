package com.healthcaremngnt.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.enums.TreatmentStatus;
import com.healthcaremngnt.exceptions.AppointmentNotFoundException;
import com.healthcaremngnt.exceptions.DataPersistenceException;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.Treatment;
import com.healthcaremngnt.repository.TreatmentRepository;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.TreatmentService;

@Service
@Transactional
public class TreatmentServiceImpl implements TreatmentService {

	private static final Logger logger = LogManager.getLogger(TreatmentServiceImpl.class);

	private final AppointmentService appointmentService;
	private final TreatmentRepository treatmentRepository;

	public TreatmentServiceImpl(AppointmentService appointmentService, TreatmentRepository treatmentRepository) {
		this.appointmentService = appointmentService;
		this.treatmentRepository = treatmentRepository;
	}

	@Override
	public Treatment createTreatment(Treatment treatment) throws AppointmentNotFoundException {
		logger.info("Creating treatment for Appointment ID: {}", treatment.getAppointment().getAppointmentID());

		validateTreatment(treatment);

		Appointment appointment = appointmentService
				.getAppointmentDetails(treatment.getAppointment().getAppointmentID());

		treatment.setAppointment(appointment);

		var savedTreatment = treatmentRepository.save(treatment);
		logger.info("Successfully saved treatment with ID: {}", savedTreatment.getTreatmentID());

		return savedTreatment;
	}

	private void validateTreatment(Treatment treatment) {
		if (treatment == null || treatment.getAppointment() == null
				|| treatment.getAppointment().getAppointmentID() == null) {
			throw new IllegalArgumentException("Invalid treatment data: Appointment details are missing.");
		}
	}

	@Override
	public Treatment getTreatmentDetails(Long treatmentID) throws DataPersistenceException {
		logger.info("Fetching treatment details for ID: {}", treatmentID);

		if (treatmentID == null || treatmentID <= 0) {
			throw new IllegalArgumentException("Invalid treatment ID.");
		}

		return treatmentRepository.findById(treatmentID)
				.orElseThrow(() -> new DataPersistenceException("Treatment not found with ID: " + treatmentID));
	}

	@Override
	public List<Treatment> getAllUnbilledTreatments() {
		logger.info("Fetching all unbilled treatments with COMPLETED status.");

		var treatments = treatmentRepository.findTreatmentsByStatus(TreatmentStatus.COMPLETED);

		if (treatments.isEmpty()) {
			logger.warn("No unbilled treatments found.");
		}

		return treatments;
	}

	@Override
	public Treatment updateTreatmentDetails(Treatment treatment) {
		logger.info("Updating treatment details for ID: {}", treatment.getTreatmentID());

		validateTreatment(treatment);

		var updatedTreatment = treatmentRepository.save(treatment);
		logger.info("Successfully updated treatment with ID: {}", updatedTreatment.getTreatmentID());

		return updatedTreatment;
	}

	@Override
	public List<Long> getPatientListByDoctor(Long doctorID) {
		logger.info("Fetching Patient IDs for Doctor ID: {}", doctorID);

		if (doctorID == null || doctorID <= 0) {
			throw new IllegalArgumentException("Invalid Doctor ID.");
		}

		return treatmentRepository.findPatientByDoctorID(doctorID);
	}

	@Override
	public List<Long> getDoctorListByPatient(Long patientID) {
		logger.info("Fetching Doctor IDs for Patient ID: {}", patientID);

		if (patientID == null || patientID <= 0) {
			throw new IllegalArgumentException("Invalid Patient ID.");
		}

		return treatmentRepository.findDoctorByPatientID(patientID);
	}

	@Override
	public List<Treatment> getTreatmentDetailsByPatient(Long patientID) {
		logger.info("Fetching treatment details for Patient ID: {}", patientID);

		if (patientID == null || patientID <= 0) {
			throw new IllegalArgumentException("Invalid Patient ID.");
		}

		return treatmentRepository.findByPatient(patientID);
	}

	@Override
	public List<Treatment> getFollowUpTreatments() {
		logger.info("Fetching all treatments with FOLLOWUP status.");

		var treatments = treatmentRepository.findFollowupTreatmentsByStatus(TreatmentStatus.FOLLOWUP);

		if (treatments.isEmpty()) {
			logger.warn("No followup treatments found.");
		}

		return treatments;
	}

}