package com.healthcaremngnt.service.impl;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.enums.TreatmentStatus;
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
	public Treatment createTreatment(Treatment treatment) throws DataPersistenceException {

		logger.info("TreatmentServiceImpl::: createTreatment()");
		logger.debug("treatment: {}", treatment);

		try {
			Optional<Appointment> appointmentOptional = appointmentService
					.getAppointmentDetails(treatment.getAppointment().getAppointmentID());

			if (appointmentOptional.isPresent()) {
				treatment.setAppointment(appointmentOptional.get());
			}
			return treatmentRepository.save(treatment);
		} catch (Exception e) {

			logger.error("Exception while saving treatment details to DB: {}", e.getMessage(), e);

			throw new DataPersistenceException("Failed to create treatment due to a data access error.", e);
		}

	}

	@Override
	public Optional<Treatment> getTreatmentDetails(Long treatmentID) {

		logger.info("TreatmentServiceImpl::: getTreatmentDetails()");

		return treatmentRepository.findById(treatmentID);
	}

	@Override
	public List<Treatment> getAllUnbilledTreatments() {

		logger.info("TreatmentServiceImpl::: getAllUnbilledTreatments()");

		return treatmentRepository.findTreatmentsByStatus(TreatmentStatus.COMPLETED);

	}

	@Override
	public Treatment updateTreatmentDetails(Treatment treatment) {

		logger.info("TreatmentServiceImpl::: updateTreatmentOnInvoice()");
		logger.debug("treatment: {}", treatment);

		return treatmentRepository.save(treatment);

	}

	@Override
	public List<Long> getTreatmentDetailsByDoctor(Long doctorID) {

		logger.info("TreatmentServiceImpl::: getTreatmentDetailsByDoctor()");

		return treatmentRepository.findPatientByDoctorID(doctorID);
	}
	
}