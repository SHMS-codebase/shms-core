package com.healthcaremngnt.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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
	public DoctorSchedule saveDoctorSchedule(DoctorSchedule doctorSchedule) throws InvalidInputException {
		logger.info("Saving doctor schedule for Doctor ID: {}", doctorSchedule.getDoctor().getDoctorID());

		validateDoctorSchedule(doctorSchedule);

		var savedSchedule = doctorScheduleRepository.save(doctorSchedule);

		logger.info("Successfully saved doctor schedule with ID: {}", savedSchedule.getScheduleID());
		return savedSchedule;
	}

	private void validateDoctorSchedule(DoctorSchedule doctorSchedule) throws InvalidInputException {
		if (doctorSchedule == null || doctorSchedule.getDoctor() == null) {
			throw new InvalidInputException("Doctor schedule or associated doctor cannot be null.");
		}

		if (doctorSchedule.getAvailableDate() == null || doctorSchedule.getStartTime() == null
				|| doctorSchedule.getEndTime() == null) {
			throw new InvalidInputException("Available date, start time, and end time must be provided.");
		}

		if (doctorSchedule.getStartTime().isAfter(doctorSchedule.getEndTime())) {
			throw new InvalidInputException("Start time cannot be after end time.");
		}
	}

	@Override
	public DoctorSchedule findScheduleDetail(Long scheduleID) throws InvalidInputException {
		logger.info("Fetching schedule details for Schedule ID: {}", scheduleID);

		return doctorScheduleRepository.findById(scheduleID)
				.orElseThrow(() -> new InvalidInputException("Schedule not found with ID: " + scheduleID));
	}

	@Override
	public List<DoctorSchedule> getAllSchedules() {
		logger.info("Fetching all doctor schedules.");

		return doctorScheduleRepository.findAll();
	}

	@Override
	public List<DoctorSchedule> getPendingSchedules() {
		logger.info("Fetching pending schedules.");

		return doctorScheduleRepository.findByScheduleStatus(ScheduleStatus.PENDING);
	}

	@Override
	public void createDoctorSchedule(DoctorScheduleRequest request)
			throws DoctorNotFoundException, InvalidInputException, OverlappingScheduleException {
		logger.info("Creating doctor schedule for Doctor ID: {}", request.getDoctorID());

		Doctor doctor = doctorService.getDoctorDetails(request.getDoctorID());

		validateDateAndTime(request.getAvailableDate(), request.getStartTime(), request.getEndTime());
		checkForOverlappingSchedules(request, request.getDoctorID());

		DoctorSchedule doctorSchedule = buildDoctorSchedule(request, doctor);
		doctorScheduleRepository.save(doctorSchedule);

		logger.info("Successfully created doctor schedule.");
	}

	private void validateDateAndTime(String availableDate, String startTime, String endTime)
			throws InvalidInputException {
		logger.info("Validating date and time for schedule.");

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
		logger.info("Checking for overlapping schedules for Doctor ID: {}", doctorID);

		LocalDate availableDate = LocalDate.parse(request.getAvailableDate());
		LocalTime startTime = LocalTime.parse(request.getStartTime());
		LocalTime endTime = LocalTime.parse(request.getEndTime());

		var overlapFound = doctorScheduleRepository.findByDoctor_DoctorIDAndAvailableDate(doctorID, availableDate)
				.stream().anyMatch(schedule -> schedule.getStartTime().isBefore(endTime)
						&& schedule.getEndTime().isAfter(startTime));

		if (overlapFound) {
			throw new OverlappingScheduleException("The schedule is overlapping with an existing schedule.");
		}
	}

	private DoctorSchedule buildDoctorSchedule(DoctorScheduleRequest request, Doctor doctor)
			throws InvalidInputException {

		logger.info("Building doctor schedule for Doctor ID: {}", doctor.getDoctorID());

		validateDoctorScheduleRequest(request);

		DoctorSchedule doctorSchedule = new DoctorSchedule();
		doctorSchedule.setDoctor(doctor);
		doctorSchedule.setAvailableDate(LocalDate.parse(request.getAvailableDate()));
		doctorSchedule.setStartTime(LocalTime.parse(request.getStartTime()));
		doctorSchedule.setEndTime(LocalTime.parse(request.getEndTime()));
		doctorSchedule.setAvailableCount(request.getAvailableCount());
		doctorSchedule.setScheduleStatus(request.getScheduleStatus());

		logger.info("Doctor schedule built successfully: {}", doctorSchedule);
		return doctorSchedule;
	}

	private void validateDoctorScheduleRequest(DoctorScheduleRequest request) throws InvalidInputException {
		if (request.getAvailableDate() == null || request.getStartTime() == null || request.getEndTime() == null) {
			throw new InvalidInputException("Available date, start time, and end time must be provided.");
		}

		LocalTime startTime = LocalTime.parse(request.getStartTime());
		LocalTime endTime = LocalTime.parse(request.getEndTime());

		if (startTime.isAfter(endTime)) {
			throw new InvalidInputException("Start time cannot be after end time.");
		}
	}

	@Override
	public void updateDoctorSchedules(List<DoctorSchedule> doctorSchedulesList, Long doctorID, Long scheduleID)
			throws InvalidInputException, DoctorNotFoundException {
		logger.info("Updating doctor schedules. Doctor ID: {}, Schedule ID: {}", doctorID, scheduleID);

		if (doctorSchedulesList == null || doctorSchedulesList.isEmpty()) {
			logger.warn("Doctor schedules list is empty or null.");
			throw new InvalidInputException("No schedules provided for update.");
		}

		Doctor doctor = getDoctorByDoctorOrScheduleID(doctorID, scheduleID);

		doctorSchedulesList.forEach(schedule -> schedule.setDoctor(doctor));

		doctorScheduleRepository.saveAll(doctorSchedulesList);
		logger.info("Doctor schedules updated successfully.");
	}

	private Doctor getDoctorByDoctorOrScheduleID(Long doctorID, Long scheduleID) throws DoctorNotFoundException {
		AtomicReference<Long> resolvedDoctorID = new AtomicReference<>(doctorID);

		if (scheduleID != null) {
			var schedule = doctorScheduleRepository.findByScheduleID(scheduleID);
			resolvedDoctorID.set(schedule.getDoctor().getDoctorID()); // Modify using `set()`
		}

		return doctorService.getDoctorDetails(resolvedDoctorID.get());
	}

	@Override
	public List<LocalDate> getAvailableDatesByDoctorID(Long doctorID) {
		logger.info("Fetching available dates for Doctor ID: {}", doctorID);

		return doctorScheduleRepository.findAvailableDatesByDoctorID(doctorID);
	}

	@Override
	public List<String> getAvailableTimeSlotsByDoctorIDAndDate(Long doctorID, LocalDate availableDate) {

		logger.info("Fetching available time slots for Doctor ID: {} on Date: {}", doctorID, availableDate);

		return doctorScheduleRepository
				.findByDoctor_DoctorIDAndAvailableDateAndAvailableCountGreaterThan(doctorID, availableDate,
						SmartHealthCareConstants.MINIMUM_SLOT)
				.stream()
				.map(schedule -> String.format("%s - %s",
						schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
						schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))))
				.collect(Collectors.toList());
	}

}