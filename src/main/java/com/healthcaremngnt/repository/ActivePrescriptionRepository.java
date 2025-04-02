package com.healthcaremngnt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.model.Prescription;
import com.healthcaremngnt.projection.ActivePrescriptionProjection;

@Repository
public interface ActivePrescriptionRepository extends JpaRepository<Prescription, Long> {

	@Query("SELECT p.prescriptionID as prescriptionID, " + "pat.patientName as patientName, "
			+ "t.diagnosis as diagnosis, "
			+ "(SELECT COUNT(pd) FROM PrescriptionDetail pd WHERE pd.prescription.prescriptionID = p.prescriptionID) as prescriptionDetailCount, "
			+ "p.prescriptionDate as prescriptionDate, " + "'ONGOING' as treatmentStatus, "
			+ "t.followUpNeeded as followUpNeeded " + "FROM Prescription p " + "JOIN p.treatment t "
			+ "JOIN Patient pat ON t.patientID = pat.patientID " + "WHERE p.prescriptionStatus = 'ACTIVE' "
			+ "AND t.doctorID = :doctorID")
	List<ActivePrescriptionProjection> findActivePrescriptionsByDoctor(@Param("doctorID") Long doctorID);

	@Query("SELECT p.prescriptionID as prescriptionID, " + "pat.patientName as patientName, "
			+ "t.diagnosis as diagnosis, "
			+ "(SELECT COUNT(pd) FROM PrescriptionDetail pd WHERE pd.prescription.prescriptionID = p.prescriptionID) as prescriptionDetailCount, "
			+ "p.prescriptionDate as prescriptionDate, " + "'ONGOING' as treatmentStatus, "
			+ "t.followUpNeeded as followUpNeeded " + "FROM Prescription p " + "JOIN p.treatment t "
			+ "JOIN Patient pat ON t.patientID = pat.patientID " + "WHERE p.prescriptionStatus = 'ACTIVE' "
			+ "AND pat.patientID = :patientID")
	List<ActivePrescriptionProjection> findActivePrescriptionsByPatient(@Param("patientID") Long patientID);

}
