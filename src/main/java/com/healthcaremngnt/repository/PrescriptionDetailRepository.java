package com.healthcaremngnt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.model.PrescriptionDetail;

@Repository
public interface PrescriptionDetailRepository extends JpaRepository<PrescriptionDetail, Long> {

	@Query("SELECT pd FROM PrescriptionDetail pd "
			+ "JOIN Prescription p ON pd.prescription.prescriptionID = p.prescriptionID "
			+ "JOIN Treatment t ON p.treatment.treatmentID = t.treatmentID "
			+ "WHERE p.prescriptionStatus = 'ACTIVE' AND t.doctorID = :doctorID")
	List<PrescriptionDetail> findActivePrescriptionsByDoctorID(@Param("doctorID") Long doctorID);

	List<PrescriptionDetail> findByPrescription_PrescriptionID(Long prescriptionID);

	List<PrescriptionDetail> findByPrescription(Prescription prescription);

}