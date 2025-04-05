package com.healthcaremngnt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.model.Prescription;

@Repository
public interface PrescriptionRespository extends JpaRepository<Prescription, Long> {

	Optional<Prescription> findByTreatment_TreatmentID(Long treatmentID);

	@Query("SELECT p FROM Prescription p WHERE " + "p.treatment.treatmentID IN (SELECT t.treatmentID FROM "
			+ "Treatment t WHERE t.patientID = :patientID)")
	List<Prescription> findPrescriptionByPatientID(Long patientID);

}