package com.healthcaremngnt.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.healthcaremngnt.exceptions.DoctorNotFoundException;
import com.healthcaremngnt.exceptions.InvalidInputException;
import com.healthcaremngnt.exceptions.OverlappingScheduleException;
import com.healthcaremngnt.model.DoctorSchedule;
import com.healthcaremngnt.model.DoctorScheduleRequest;

public interface DoctorScheduleService {

	DoctorSchedule saveDoctorSchedule(DoctorSchedule doctorSchedule);

	Optional<DoctorSchedule> findScheduleDetail(Long scheduleID);

	List<DoctorSchedule> getAllSchedules();

	List<DoctorSchedule> getPendingSchedules();

	void createDoctorSchedule(DoctorScheduleRequest request)
			throws InvalidInputException, DoctorNotFoundException, OverlappingScheduleException;

	void updateDoctorSchedules(List<DoctorSchedule> doctorSchedulesList, Long doctorID, Long scheduleID)
			throws DoctorNotFoundException;

	List<LocalDate> getAvailableDatesByDoctorID(Long doctorID);

	List<String> getAvailableTimeSlotsByDoctorIDAndDate(Long doctorID, LocalDate localDate);

}