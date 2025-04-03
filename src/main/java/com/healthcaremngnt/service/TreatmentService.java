package com.healthcaremngnt.service;

import java.util.List;

import com.healthcaremngnt.exceptions.AppointmentNotFoundException;
import com.healthcaremngnt.exceptions.DataPersistenceException;
import com.healthcaremngnt.model.Treatment;

public interface TreatmentService {

	Treatment createTreatment(Treatment treatment) throws DataPersistenceException, AppointmentNotFoundException;

	Treatment getTreatmentDetails(Long treatmentID) throws DataPersistenceException;

	List<Treatment> getAllUnbilledTreatments();

	Treatment updateTreatmentDetails(Treatment treatment);

	List<Long> getTreatmentDetailsByDoctor(Long doctorID);
	
}