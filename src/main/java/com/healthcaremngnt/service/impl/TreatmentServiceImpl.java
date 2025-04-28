package com.healthcaremngnt.service.impl;

import java.util.ArrayList;
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
		logger.info("Creating treatment for Appointment ID: {}", treatment.getAppointments().get(0).getAppointmentID());

		validateTreatment(treatment);

		// Get the latest appointment that was provided when creating the treatment
		Appointment latestAppointment = appointmentService
				.getAppointmentDetails(treatment.getAppointments().get(0).getAppointmentID());

		// Initialize the appointments list if it's null
		if (treatment.getAppointments() == null) {
			treatment.setAppointments(new ArrayList<>());
		}

		// Clear any existing appointments and add the latest one
		treatment.getAppointments().clear();
		treatment.getAppointments().add(latestAppointment);

		Treatment savedTreatment = treatmentRepository.save(treatment);
		logger.info("Successfully saved treatment with ID: {}", savedTreatment.getTreatmentID());

		return savedTreatment;
	}

	private void validateTreatment(Treatment treatment) {
		if (treatment == null || treatment.getAppointments() == null
				|| treatment.getAppointments().get(0).getAppointmentID() == null) {
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

		List<Treatment> treatments = treatmentRepository.findTreatmentsByStatus(TreatmentStatus.COMPLETED);

		if (treatments.isEmpty()) {
			logger.warn("No unbilled treatments found.");
		}

		return treatments;
	}

	@Override
	public Treatment updateTreatmentDetails(Treatment treatment) {
		logger.info("Updating treatment details for ID: {}", treatment.getTreatmentID());

		validateTreatment(treatment);

		Treatment updatedTreatment = treatmentRepository.save(treatment);
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

}