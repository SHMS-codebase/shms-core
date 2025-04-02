package com.healthcaremngnt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.MedicalHistory;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {

	List<MedicalHistory> findByPatient(Patient patient);

	// Example of a derived query (if needed):
	// List<MedicalHistory> findByPatient_PatientID(Long patientID); // Find by patient ID

	// Example using Pageable for pagination (if needed)
	/*
	 * @Query("SELECT mh FROM MedicalHistory mh WHERE ...") // Your query if needed
	 * Page<MedicalHistory> findMedicalHistoriesWithPagination(Pageable pageable);
	 */

}