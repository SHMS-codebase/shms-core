package com.healthcaremngnt.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.enums.AppointmentStatus;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.Treatment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	Appointment findByAppointmentID(Long appointmentID);

	List<Appointment> findByAppointmentDate(LocalDate appointmentDate);

	List<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate);

	List<Appointment> findByAppointmentStatusAndAppointmentDateGreaterThanEqualAndAppointmentDateLessThanEqual(AppointmentStatus appointmentStatus,
			LocalDate startDate, LocalDate endDate);

	List<Appointment> findByDoctorAndAppointmentDateAndAppointmentStatus(Doctor doctor, LocalDate appointmentDate,
			AppointmentStatus appointmentStatus);

	@Query("SELECT a FROM Appointment a " + "JOIN a.patient p " + "JOIN a.doctor d "
			+ "WHERE (:patientID IS NULL OR p.patientID = :patientID) "
			+ "AND (:doctorID IS NULL OR d.doctorID = :doctorID) "
			+ "AND (:appointmentDate IS NULL OR a.appointmentDate = :appointmentDate) "
			+ "AND (:appointmentStatus IS NULL OR a.appointmentStatus = :appointmentStatus)")
	List<Appointment> findAppointments(@Param("patientID") Long patientID, @Param("doctorID") Long doctorID,
			@Param("appointmentDate") LocalDate appointmentDate,
			@Param("appointmentStatus") AppointmentStatus appointmentStatus);

	List<Appointment> findByPatientAndAppointmentStatusAndAppointmentDateGreaterThanEqual(Patient patient,
			AppointmentStatus appointmentStatus, LocalDate appointmentDate);

	@Modifying
	@Query("UPDATE Appointment a SET a.appointmentStatus = :status, a.treatment = :treatment WHERE a.appointmentID = :id")
	void updateAppointmentStatusAndTreatment(@Param("id") Long appointmentID,
			@Param("status") AppointmentStatus appointmentStatus, @Param("treatment") Treatment treatment);

	// Need to qrite the query for cancelled appointments

	List<Appointment> findByAppointmentStatus(AppointmentStatus appointmentStatus);

	// Example of a derived query (if needed):
	// List<Appointment> findByPatient_PatientID(Long patientID);
	// Find by patient ID using Spring Data JPA's derived query feature.

	// Example of a custom query with pagination (need for Batch Processes)
	/*
	 * @Query("SELECT a FROM Appointment a WHERE ...") Page<Appointment>
	 * findAppointmentsWithPagination(Pageable pageable);
	 */

}