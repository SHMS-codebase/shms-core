package com.healthcaremngnt.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.constants.SmartHealthCareConstants;
import com.healthcaremngnt.enums.ScheduleStatus;
import com.healthcaremngnt.exceptions.DoctorNotFoundException;
import com.healthcaremngnt.exceptions.InvalidInputException;
import com.healthcaremngnt.exceptions.OverlappingScheduleException;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.DoctorSchedule;
import com.healthcaremngnt.model.DoctorScheduleRequest;
import com.healthcaremngnt.repository.DoctorScheduleRepository;
import com.healthcaremngnt.service.DoctorScheduleService;
import com.healthcaremngnt.service.DoctorService;

@Service
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

	private static final Logger logger = LogManager.getLogger(DoctorScheduleServiceImpl.class);

	private DoctorService doctorService;

	private DoctorScheduleRepository doctorScheduleRepository;

	public DoctorScheduleServiceImpl(DoctorService doctorService, DoctorScheduleRepository doctorScheduleRepository) {
		this.doctorService = doctorService;
		this.doctorScheduleRepository = doctorScheduleRepository;
	}

	@Override
	public DoctorSchedule saveDoctorSchedule(DoctorSchedule doctorSchedule) {
		logger.info("DoctorScheduleServiceImpl::: saveDoctorSchedule()");
		return doctorScheduleRepository.save(doctorSchedule);
	}

	@Override
	public Optional<DoctorSchedule> findScheduleDetail(Long scheduleID) {
		logger.info("DoctorScheduleServiceImpl::: findScheduleDetail()");
		return doctorScheduleRepository.findById(scheduleID);
	}

	@Override
	public List<DoctorSchedule> getAllSchedules() {
		logger.info("DoctorScheduleServiceImpl::: getAllSchedules()");
		return (List<DoctorSchedule>) doctorScheduleRepository.findAll();
	}

	@Override
	public List<DoctorSchedule> getPendingSchedules() {
		logger.info("DoctorScheduleServiceImpl::: getPendingSchedules()");
//		String scheduleStatus = ScheduleStatus.PENDING.name();
		ScheduleStatus scheduleStatus = ScheduleStatus.PENDING;
		return doctorScheduleRepository.findByScheduleStatus(scheduleStatus);
	}

	@Override
	public void createDoctorSchedule(DoctorScheduleRequest request)
			throws InvalidInputException, DoctorNotFoundException, OverlappingScheduleException {

		logger.info("DoctorScheduleServiceImpl::: createDoctorSchedule()");

		try { // Add a try-catch block for proper exception handling

			Long doctorID = request.getDoctorID();

			Optional<Doctor> doctorOptional = doctorService.getDoctorDetails(doctorID);

			Doctor doctor = doctorOptional.orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
			validateDateAndTime(request.getAvailableDate(), request.getStartTime(), request.getEndTime());

			checkForOverlappingSchedules(request, doctorID); 

			DoctorSchedule doctorSchedule = buildDoctorSchedule(request, doctor);
			doctorScheduleRepository.save(doctorSchedule);

		} catch (DoctorNotFoundException | InvalidInputException | OverlappingScheduleException e) {
			logger.error("Error creating doctor schedule: {}", e.getMessage());
			throw e; // Re-throw the specific exception
		} catch (Exception e) {
			logger.error("Unexpected error creating doctor schedule: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to create doctor schedule", e); // Wrap and re-throw
		}
	}

	private void validateDateAndTime(String availableDate, String startTime, String endTime)
			throws InvalidInputException {

		logger.info("DoctorScheduleServiceImpl::: validateDateAndTime()");

		try {
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
			LocalDate.parse(availableDate, dateFormatter);
			LocalTime parsedStartTime = LocalTime.parse(startTime, timeFormatter);
			LocalTime parsedEndTime = LocalTime.parse(endTime, timeFormatter);

			if (parsedStartTime.isBefore(LocalTime.of(10, 0)) || parsedEndTime.isAfter(LocalTime.of(21, 0))) {
				throw new InvalidInputException("Time slots must be between 10 AM and 9 PM.");
			}
		} catch (DateTimeParseException e) {
			throw new InvalidInputException("Invalid date or time format.", e);
		}
	}

	private void checkForOverlappingSchedules(DoctorScheduleRequest request, Long doctorID)
			throws OverlappingScheduleException {

		logger.info("DoctorScheduleServiceImpl::: checkForOverlappingSchedules()");

		LocalDate availableDateForm = LocalDate.parse(request.getAvailableDate());
		LocalTime startTimeForm = LocalTime.parse(request.getStartTime());
		LocalTime endTimeForm = LocalTime.parse(request.getEndTime());

		List<DoctorSchedule> existingSchedules = doctorScheduleRepository
				.findByDoctor_DoctorIDAndAvailableDate(doctorID, availableDateForm);

		boolean overlapFound = existingSchedules.stream().anyMatch(schedule -> {
			LocalTime startTimeDB = schedule.getStartTime();
			LocalTime endTimeDB = schedule.getEndTime();

			return (startTimeForm.isBefore(endTimeDB) && startTimeForm.isAfter(startTimeDB))
					|| (endTimeForm.isBefore(endTimeDB) && endTimeForm.isAfter(startTimeDB))
					|| (startTimeForm.isBefore(startTimeDB) && endTimeForm.isAfter(endTimeDB))
					|| startTimeForm.equals(startTimeDB) || endTimeForm.equals(endTimeDB);
		});

		if (overlapFound) {
			throw new OverlappingScheduleException(
					"The schedule is overlapping with an existing Schedule. Please create a schedule with no overlapping.");
		}
	}

	private DoctorSchedule buildDoctorSchedule(DoctorScheduleRequest request, Doctor doctor) {

		logger.info("DoctorScheduleServiceImpl::: buildDoctorSchedule()");

		DoctorSchedule doctorSchedule = new DoctorSchedule();
		doctorSchedule.setDoctor(doctor);
		doctorSchedule.setAvailableDate(LocalDate.parse(request.getAvailableDate()));
		doctorSchedule.setStartTime(LocalTime.parse(request.getStartTime()));
		doctorSchedule.setEndTime(LocalTime.parse(request.getEndTime()));
//		doctorSchedule.setAvailableCount(SmartHealthCareConstants.MAXIMUM_SLOTS);
		doctorSchedule.setAvailableCount(request.getAvailableCount());
		doctorSchedule.setScheduleStatus(request.getScheduleStatus());

		return doctorSchedule;
	}

	@Override
	public void updateDoctorSchedules(List<DoctorSchedule> doctorSchedulesList, Long doctorID, Long scheduleID)
			throws DoctorNotFoundException {

		logger.info("DoctorScheduleServiceImpl::: updateDoctorSchedules()");

		try {
			if (doctorSchedulesList != null && !doctorSchedulesList.isEmpty()) {

				doctorSchedulesList.forEach(doctorSchedule -> {
					logger.debug("ScheduleID: {}", doctorSchedule.getScheduleID());
					logger.debug("AvailableDate: {}", doctorSchedule.getAvailableDate());
					logger.debug("StartTime: {}", doctorSchedule.getStartTime());
					logger.debug("EndTime: {}", doctorSchedule.getEndTime());
					logger.debug("ScheduleStatus: {}", doctorSchedule.getScheduleStatus());
					logger.debug("createdDate: {}", doctorSchedule.getCreatedDate());
					logger.debug("Doctor: {}", doctorSchedule.getDoctor());
				});

				Doctor doctorDetails = new Doctor();

				if (scheduleID != null) {
					logger.debug("Get the Doctor details using the scheduleID: {}", scheduleID);
					DoctorSchedule schedule = doctorScheduleRepository.findByScheduleID(scheduleID);
					doctorID = schedule.getDoctor().getDoctorID();
				}

				if (doctorID != null) {
					logger.debug("Get the Doctor details using the doctorID: {}", doctorID);
					Optional<Doctor> doctorOptionals = doctorService.getDoctorDetails(doctorID);
					if (doctorOptionals.isPresent()) {
						doctorDetails = doctorOptionals.get();
						logger.debug("doctorDetails: {}", doctorDetails);
					} else {
						throw new DoctorNotFoundException("Doctor with ID " + doctorID + " not found");
					}

				}

				for (DoctorSchedule schedule : doctorSchedulesList) {
					logger.debug("schedule: {}", schedule);
					schedule.setDoctor(doctorDetails);
				}
				logger.debug("doctorSchedulesList: {}", doctorSchedulesList);
				doctorScheduleRepository.saveAll(doctorSchedulesList);
			} else {
				logger.warn("doctorSchedulesList is null or empty");
			}
		} catch (IllegalArgumentException e) {
			logger.error("Validation failed: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("An unexpected error occurred: {}", e.getMessage());
			throw new RuntimeException("Failed to update doctor schedule", e);
		}

	}

	@Override
	public List<LocalDate> getAvailableDatesByDoctorID(Long doctorID) {
		logger.info("DoctorScheduleServiceImpl::: getAvailableDatesByDoctorID()");
		return doctorScheduleRepository.findAvailableDatesByDoctorID(doctorID);
	}

	@Override
	public List<String> getAvailableTimeSlotsByDoctorIDAndDate(Long doctorID, LocalDate availableDate) {

		logger.info("DoctorScheduleServiceImpl::: getAvailableTimeSlotsByDoctorIDAndDate()");
		
		int availableCount = SmartHealthCareConstants.MINIMUM_SLOT;

		return doctorScheduleRepository.findByDoctor_DoctorIDAndAvailableDateAndAvailableCountGreaterThan(doctorID, availableDate, availableCount).stream()
				.map(schedule -> {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
					return schedule.getStartTime().format(formatter) + " - " + schedule.getEndTime().format(formatter);
				}).collect(Collectors.toList());
	}

}