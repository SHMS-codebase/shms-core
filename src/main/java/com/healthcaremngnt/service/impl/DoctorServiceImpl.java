package com.healthcaremngnt.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.healthcaremngnt.enums.ScheduleStatus;
import com.healthcaremngnt.exceptions.DoctorNotFoundException;
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
	public Doctor register(Doctor doctor) {

		logger.info("Registering doctor: {}", doctor);

		return doctorRepository.save(doctor);
	}

	@Override
	public Optional<Doctor> getDoctorInfoCard(String userName) {

		logger.info("Retrieving doctor info card for user: {}", userName);

		Optional<User> user = userRepository.findByUserName(userName);
		return user.flatMap(u -> doctorRepository.findByUser(u));
	}

	@Override
	public List<DoctorSchedule> findDoctorSchedule(Long doctorID) throws DoctorNotFoundException {

		logger.info("Finding doctor schedule for doctor ID: {}", doctorID);

		Doctor doctor = doctorRepository.findById(doctorID)
				.orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));

		return doctorScheduleRepository.findByDoctor(doctor);
	}

	@Override
	public List<Doctor> getAllDoctors() {

		logger.info("Retrieving all doctors from DB");

		return doctorRepository.findAll();
	}

	@Override
	public Optional<Doctor> getDoctorDetails(Long doctorID) throws DoctorNotFoundException {

		logger.info("Retrieving Doctor Details for the doctorID, {}", doctorID);

		return doctorRepository.findById(doctorID);
	}

	@Override
	public DoctorScheduleWrapper getDoctorScheduleWrapper(Long doctorID, Long scheduleID)
			throws DoctorNotFoundException {

		logger.info("DoctorServiceImpl::: getDoctorScheduleWrapper()");

		DoctorScheduleWrapper doctorScheduleWrapper = new DoctorScheduleWrapper();

		if (scheduleID != null) {
			Optional<DoctorSchedule> schedule = doctorScheduleRepository.findById(scheduleID);
			doctorScheduleWrapper.setDoctorScheduleList(schedule.map(List::of).orElse(List.of()));
		} else {
			doctorScheduleWrapper.setDoctorScheduleList(findDoctorSchedule(doctorID));
		}

		return doctorScheduleWrapper;
	}

	@Override
	public void loadDoctorsAndFormValues(Model model, Long doctorID, LocalDate availableDate, LocalTime startTime,
			LocalTime endTime, String scheduleStatus) {

		logger.info("DoctorServiceImpl::: loadDoctorsAndFormValues()");
		
		List<Doctor> doctors = getAllDoctors();

		// Filter doctors list if doctorID is present
		if (doctorID != null) {
			logger.debug("The Create Schedule is called from Doctor Profile!!");
			doctors = doctors.stream().filter(doc -> doc.getDoctorID().equals(doctorID)).collect(Collectors.toList());
		}

		model.addAttribute("doctors", doctors);

		model.addAttribute("doctorID", doctorID);
		logger.debug("doctorID: {}", doctorID);
		
		model.addAttribute("availableDate", availableDate != null ? availableDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null); 
		model.addAttribute("startTime", startTime != null ? startTime.format(DateTimeFormatter.ofPattern("HH:mm")) : null); 
		model.addAttribute("endTime", endTime != null ? endTime.format(DateTimeFormatter.ofPattern("HH:mm")) : null); 
		model.addAttribute("scheduleStatus", scheduleStatus);
	}

	@Override
	public List<Doctor> getDoctorsWithSchedule() {

		logger.info("Retrieving Doctors with Valid Schedules");
		
		ScheduleStatus scheduleStatus = ScheduleStatus.APPROVED;
		return doctorRepository.findDoctorsByScheduleStatus(scheduleStatus);
	}

}