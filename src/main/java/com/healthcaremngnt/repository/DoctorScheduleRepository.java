package com.healthcaremngnt.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.healthcaremngnt.enums.ScheduleStatus;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.DoctorSchedule;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

	@Query("SELECT ds FROM DoctorSchedule ds JOIN ds.doctor d " + "WHERE (:doctorID IS NULL OR d.doctorID = :doctorID) "
			+ "AND (:availableDate IS NULL OR ds.availableDate = :availableDate) "
			+ "AND (:specialization IS NULL OR d.specialization = :specialization) "
			+ "AND (:scheduleStatus IS NULL OR ds.scheduleStatus = :scheduleStatus)")
	List<DoctorSchedule> findSchedules(@Param("doctorID") Long doctorID,
			@Param("availableDate") LocalDate availableDate, @Param("specialization") String specialization,
			@Param("scheduleStatus") ScheduleStatus scheduleStatus);

	List<DoctorSchedule> findByDoctor(Doctor doctor);

	DoctorSchedule findByScheduleID(Long scheduleID);
	// If findByScheduleID is just an alias for findById, remove it and use the
	// inherited findById
	// DoctorSchedule findByScheduleID(Long scheduleID); <- Remove if redundant

	List<DoctorSchedule> findByScheduleStatus(ScheduleStatus scheduleStatus);

	@Query("SELECT DISTINCT ds.availableDate FROM DoctorSchedule ds JOIN ds.doctor d "
			+ "WHERE d.doctorID = :doctorID AND ds.scheduleStatus = 'Approved' AND ds.availableCount > 0")
	List<LocalDate> findAvailableDatesByDoctorID(@Param("doctorID") Long doctorID);

	List<DoctorSchedule> findByDoctor_DoctorIDAndAvailableDate(Long doctorID, LocalDate availableDate); // Derived Query
	
	List<DoctorSchedule> findByDoctor_DoctorIDAndAvailableDateAndAvailableCountGreaterThan(Long doctorID, LocalDate availableDate, int availableCount);

	@Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.doctorID = :doctorID AND ds.availableDate = :availableDate "
			+ "AND ds.startTime = :startTime AND ds.endTime = :endTime")
	Optional<DoctorSchedule> findByDoctor_DoctorIDAndAvailableDateAndTimeSlot(@Param("doctorID") Long doctorID,
			@Param("availableDate") LocalDate availableDate, @Param("startTime") LocalTime startTime,
			@Param("endTime") LocalTime endTime);

//	@Modifying(clearAutomatically = true)
//	@Query("UPDATE DoctorSchedule ds SET status = 'EXPIRED' WHERE ds.availableDate < CURRENT_DATE)
//	int expireOutdatedSchedules();
	
	// Example using Pageable for pagination (if needed)
    /*
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ...")
    Page<DoctorSchedule> findSchedulesWithPagination(@Param("doctorId") Long doctorId, ..., Pageable pageable);
    */

}