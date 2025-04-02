package com.healthcaremngnt.service;

import java.util.List;
import java.util.Optional;

import com.healthcaremngnt.exceptions.DataPersistenceException;
import com.healthcaremngnt.model.Treatment;

public interface TreatmentService {

	Treatment createTreatment(Treatment treatment) throws DataPersistenceException;

	Optional<Treatment> getTreatmentDetails(Long treatmentID);

	List<Treatment> getAllUnbilledTreatments();

	Treatment updateTreatmentDetails(Treatment treatment);

	List<Long> getTreatmentDetailsByDoctor(Long doctorID);
	
}