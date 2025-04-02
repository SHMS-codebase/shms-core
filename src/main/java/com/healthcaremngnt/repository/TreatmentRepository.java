package com.healthcaremngnt.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.enums.TreatmentStatus;
import com.healthcaremngnt.model.Treatment;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

	List<Treatment> findByDoctorID(Long doctorID);

	@Query("SELECT t FROM Treatment t WHERE t.invoiceGenerated = false AND t.treatmentStatus = :status")
	List<Treatment> findTreatmentsByStatus(@Param("status") TreatmentStatus status);

	@Query("SELECT t FROM Treatment t WHERE (:patientID IS NULL OR t.patientID = :patientID) "
			+ "AND (:treatmentID IS NULL OR t.treatmentID = :treatmentID) "
			+ "AND (:treatmentDate IS NULL OR FUNCTION('DATE', t.treatmentDate) = :treatmentDate) "
			+ "AND (:treatmentStatus IS NULL OR t.treatmentStatus = :treatmentStatus)")
	List<Treatment> findTreatments(Long patientID, Long treatmentID, LocalDate treatmentDate,
			TreatmentStatus treatmentStatus);

	@Query("SELECT DISTINCT t.patientID FROM Treatment t WHERE t.doctorID = :doctorID")
	List<Long> findPatientByDoctorID(Long doctorID);

	// Example of derived queries (if needed):
	// Optional<Treatment> findByDiagnosis(String diagnosis);

	// List<Treatment> findByAppointment_AppointmentID(Long appointmentId); // Find
	// by appointment ID

	// Example using Pageable for pagination (if needed)
	/*
	 * @Query("SELECT t FROM Treatment t WHERE ...") // Your query if needed
	 * Page<Treatment> findTreatmentsWithPagination(Pageable pageable);
	 */

}