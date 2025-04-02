package com.healthcaremngnt.service;

import java.util.List;

import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.PrescriptionDetail;

public interface PrescriptionDetailService {

	List<PrescriptionDetail> createPrescriptionDetails(Prescription prescription, List<PrescriptionDetail> prescriptionDetails);

	List<PrescriptionDetail> getActivePrescriptions(Doctor doctor);

	List<PrescriptionDetail> getPrescriptionDetails(Long prescriptionID);

	void updatePrescriptionDetails(Prescription prescription);

}