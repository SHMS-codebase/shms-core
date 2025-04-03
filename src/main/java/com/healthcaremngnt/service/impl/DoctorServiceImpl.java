package com.healthcaremngnt.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.healthcaremngnt.enums.ScheduleStatus;
import com.healthcaremngnt.exceptions.DoctorNotFoundException;
import com.healthcaremngnt.exceptions.InvalidInputException;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.DoctorSchedule;
import com.healthcaremngnt.model.DoctorScheduleWrapper;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.repository.DoctorRepository;
import com.healthcaremngnt.repository.DoctorScheduleRepository;
import com.healthcaremngnt.repository.UserRepository;
import com.healthcaremngnt.service.DoctorService;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

	private static final Logger logger = LogManager.getLogger(DoctorServiceImpl.class);

	private final UserRepository userRepository;
	private final DoctorRepository doctorRepository;
	private final DoctorScheduleRepository doctorScheduleRepository;

	public DoctorServiceImpl(UserRepository userRepository, DoctorRepository doctorRepository,
			DoctorScheduleRepository doctorScheduleRepository) {
		this.userRepository = userRepository;
		this.doctorRepository = doctorRepository;
		this.doctorScheduleRepository = doctorScheduleRepository;
	}

	@Override
	public Doctor register(Doctor doctor) throws InvalidInputException {
		logger.info("Registering doctor: {}", doctor);

		if (doctor == null) {
			throw new InvalidInputException("Doctor object cannot be null.");
		}

		Doctor savedDoctor = doctorRepository.save(doctor);
		logger.info("Doctor registered successfully with ID: {}", savedDoctor.getDoctorID());

		return savedDoctor;
	}

	@Override
	public Doctor getDoctorInfoCard(String userName) throws DoctorNotFoundException {
		logger.info("Retrieving doctor info card for user: {}", userName);

		User user = userRepository.findByUserName(userName)
				.orElseThrow(() -> new DoctorNotFoundException("User not found with username: " + userName));

		return doctorRepository.findByUser(user).orElseThrow(
				() -> new DoctorNotFoundException("Doctor information not found for username: " + userName));
	}

	@Override
	public List<DoctorSchedule> findDoctorSchedule(Long doctorID) throws DoctorNotFoundException {
		logger.info("Finding schedule for Doctor ID: {}", doctorID);

		Doctor doctor = doctorRepository.findById(doctorID)
				.orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorID));

		List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctor(doctor);
		logger.info("Found {} schedules for Doctor ID: {}", schedules.size(), doctorID);

		return schedules;
	}

	@Override
	public List<Doctor> getAllDoctors() {
		logger.info("Fetching all doctors from database.");

		return doctorRepository.findAll();
	}

	@Override
	public Doctor getDoctorDetails(Long doctorID) throws DoctorNotFoundException {
		logger.info("Retrieving details for Doctor ID: {}", doctorID);

		return doctorRepository.findById(doctorID)
				.orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorID));
	}

	@Override
	public DoctorScheduleWrapper getDoctorScheduleWrapper(Long doctorID, Long scheduleID)
			throws DoctorNotFoundException {
		logger.info("Fetching doctor schedule wrapper for Doctor ID: {} and Schedule ID: {}", doctorID, scheduleID);

		DoctorScheduleWrapper doctorScheduleWrapper = new DoctorScheduleWrapper();

		doctorScheduleWrapper.setDoctorScheduleList(scheduleID != null
				? List.of(doctorScheduleRepository.findById(scheduleID)
						.orElseThrow(() -> new DoctorNotFoundException("Schedule not found with ID: " + scheduleID)))
				: findDoctorSchedule(doctorID));

		return doctorScheduleWrapper;
	}

	@Override
	public void loadDoctorsAndFormValues(Model model, Long doctorID, LocalDate availableDate, LocalTime startTime,
			LocalTime endTime, String scheduleStatus) {
		logger.info("Loading doctors and form values for Doctor ID: {}", doctorID);

		List<Doctor> doctors = doctorID != null ? doctorRepository.findAll().stream()
				.filter(doc -> doc.getDoctorID().equals(doctorID)).collect(Collectors.toList())
				: doctorRepository.findAll();

		model.addAttribute("doctors", doctors);
		model.addAttribute("doctorID", doctorID);
		model.addAttribute("availableDate", formatDate(availableDate));
		model.addAttribute("startTime", formatTime(startTime));
		model.addAttribute("endTime", formatTime(endTime));
		model.addAttribute("scheduleStatus", scheduleStatus);

		logger.info("Form values loaded successfully.");
	}

	private String formatDate(LocalDate date) {
		return date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
	}

	private String formatTime(LocalTime time) {
		return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm")) : null;
	}

	@Override
	public List<Doctor> getDoctorsWithSchedule() {
		logger.info("Fetching doctors with valid schedules.");
		
		return doctorRepository.findDoctorsByScheduleStatus(ScheduleStatus.APPROVED);
	}

}