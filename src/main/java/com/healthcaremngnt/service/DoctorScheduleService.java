package com.healthcaremngnt.service;

import java.time.LocalDate;
import java.util.List;

import com.healthcaremngnt.exceptions.DoctorNotFoundException;
import com.healthcaremngnt.exceptions.InvalidInputException;
import com.healthcaremngnt.exceptions.OverlappingScheduleException;
import com.healthcaremngnt.model.DoctorSchedule;
import com.healthcaremngnt.model.DoctorScheduleRequest;

public interface DoctorScheduleService {

	DoctorSchedule saveDoctorSchedule(DoctorSchedule doctorSchedule) throws InvalidInputException;

	DoctorSchedule findScheduleDetail(Long scheduleID) throws InvalidInputException;

	List<DoctorSchedule> getAllSchedules();

	List<DoctorSchedule> getPendingSchedules();

	void createDoctorSchedule(DoctorScheduleRequest request)
			throws InvalidInputException, DoctorNotFoundException, OverlappingScheduleException;

	void updateDoctorSchedules(List<DoctorSchedule> doctorSchedulesList, Long doctorID, Long scheduleID)
			throws DoctorNotFoundException, InvalidInputException;

	List<LocalDate> getAvailableDatesByDoctorID(Long doctorID);

	List<String> getAvailableTimeSlotsByDoctorIDAndDate(Long doctorID, LocalDate localDate);

}