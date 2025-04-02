package com.healthcaremngnt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.enums.ScheduleStatus;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.User;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

	Optional<Doctor> findByUser(User user);

	Doctor findByDoctorID(Long doctorID);
	// If findByDoctorID is just an alias for findById, remove it and use the inherited findById
    // Doctor findByDoctorID(Long doctorID);  <- Remove if redundant

	@Query("SELECT d FROM Doctor d WHERE " + "(:name IS NULL OR d.doctorName LIKE %:name%) "
			+ "AND (:emailID IS NULL OR d.user.emailID LIKE %:emailID%) "
			+ "AND (:contactNumber IS NULL OR d.contactNumber LIKE %:contactNumber%) "
			+ "AND (:specialization IS NULL OR d.specialization LIKE %:specialization%)")
	List<Doctor> searchDoctors(@Param("name") String name, @Param("emailID") String emailID,
			@Param("contactNumber") String contactNumber, @Param("specialization") String specialization);
	
	@Query("SELECT d FROM Doctor d WHERE d.id IN (SELECT ds.doctor.id FROM DoctorSchedule ds WHERE ds.scheduleStatus = :scheduleStatus)")
	List<Doctor> findDoctorsByScheduleStatus(@Param("scheduleStatus") ScheduleStatus scheduleStatus);
	
	// Example of a derived query (if needed) - Finding doctors by specialization
    // List<Doctor> findBySpecialization(String specialization);

    // Example using a Pageable for pagination (if needed)
    /*
    @Query("SELECT d FROM Doctor d WHERE ...")
    Page<Doctor> searchDoctorsWithPagination(@Param("name") String name, ..., Pageable pageable);
    */

}