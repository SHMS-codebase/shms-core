package com.healthcaremngnt.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.ui.Model;

import com.healthcaremngnt.exceptions.DoctorNotFoundException;
import com.healthcaremngnt.exceptions.InvalidInputException;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.DoctorSchedule;
import com.healthcaremngnt.model.DoctorScheduleWrapper;

public interface DoctorService {

	Doctor register(Doctor doctor) throws InvalidInputException;

	Doctor getDoctorInfoCard(String userName) throws DoctorNotFoundException;

	List<DoctorSchedule> findDoctorSchedule(Long doctorID) throws DoctorNotFoundException;

	List<Doctor> getAllDoctors();

	Doctor getDoctorDetails(Long doctorID) throws DoctorNotFoundException;

	DoctorScheduleWrapper getDoctorScheduleWrapper(Long doctorID, Long scheduleID) throws DoctorNotFoundException;

	void loadDoctorsAndFormValues(Model model, Long doctorId, LocalDate availableDate, LocalTime startTime,
			LocalTime endTime, String scheduleStatus); 

	List<Doctor> getDoctorsWithSchedule();

}