package com.healthcaremngnt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.User;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

	Optional<Patient> findByUser(User user);

	@Query("SELECT p FROM Patient p WHERE " + "(:name IS NULL OR p.patientName LIKE %:name%) "
			+ "AND (:emailID IS NULL OR p.user.emailID LIKE %:emailID%) "
			+ "AND (:contactNumber IS NULL OR p.contactNumber LIKE %:contactNumber%)")
	List<Patient> searchPatients(@Param("name") String name, @Param("emailID") String emailID,
			@Param("contactNumber") String contactNumber);

	Optional<Patient> findByPatientID(Long patientID);
	// If findByPatientID is just an alias for findById, remove it and use the inherited findById
    // Optional<Patient> findByPatientID(Long id);  <- Remove if redundant, use findById

    // Example of a derived query (if needed):
    // Optional<Patient> findByContactNumber(String contactNumber);

    // Example using Pageable for pagination (if needed)
    /*
    @Query("SELECT p FROM Patient p WHERE ...")
    Page<Patient> searchPatientsWithPagination(@Param("name") String name, ..., Pageable pageable);
    */

}