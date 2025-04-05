package com.healthcaremngnt.service;

import java.util.List;

import com.healthcaremngnt.exceptions.PrescriptionNotFoundException;
import com.healthcaremngnt.model.ActivePrescription;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.PrescriptionDetail;

public interface PrescriptionService {

	Prescription createPrescription(Prescription prescription, List<PrescriptionDetail> prescriptionDetails);

	List<ActivePrescription> getActivePrescriptions(Doctor doctor);

	Prescription getPrescriptionDetails(Long prescriptionID) throws PrescriptionNotFoundException;

	void updatePrescriptionDetails(Prescription prescription) throws PrescriptionNotFoundException;

	List<ActivePrescription> getActivePrescriptions(Patient patient);

	Prescription getPrescriptionDetailsByTreatment(Long treatmentID) throws PrescriptionNotFoundException;

	List<Prescription> getPrescriptionByPatientID(Long patientID);

}