package com.healthcaremngnt.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.constants.SmartHealthCareConstants;
import com.healthcaremngnt.enums.ScheduleStatus;
import com.healthcaremngnt.exceptions.DoctorNotFoundException;
import com.healthcaremngnt.exceptions.InvalidInputException;
import com.healthcaremngnt.exceptions.OverlappingScheduleException;
import com.healthcaremngnt.model.ActivePrescription;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.DoctorSchedule;
import com.healthcaremngnt.model.DoctorScheduleRequest;
import com.healthcaremngnt.model.DoctorScheduleWrapper;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.DoctorScheduleService;
import com.healthcaremngnt.service.DoctorService;
import com.healthcaremngnt.service.PrescriptionService;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

	private static final Logger logger = LogManager.getLogger(DoctorController.class);

	private final DoctorService doctorService;
	private final DoctorScheduleService doctorScheduleService;
	private final AppointmentService appointmentService;
	private final PrescriptionService prescriptionService;

	public DoctorController(DoctorService doctorService, DoctorScheduleService doctorScheduleService,
			AppointmentService appointmentService, @Lazy PrescriptionService prescriptionService) {
		this.doctorService = doctorService;
		this.doctorScheduleService = doctorScheduleService;
		this.appointmentService = appointmentService;
		this.prescriptionService = prescriptionService;
	}

	@GetMapping("/doctordashboard")
	public String getDoctorDashboard(@SessionAttribute(RequestParamConstants.USER_NAME) String userName, Model model) {
		logger.info("Loading Doctor Dashboard");
		Optional<Doctor> doctorOptional = doctorService.getDoctorInfoCard(userName);
		Doctor doctor = new Doctor();

		if (doctorOptional.isPresent()) {
			doctor = doctorOptional.get();
			logger.debug("Doctor Details: {}", doctor);
			model.addAttribute("doctor", doctor);

			List<Appointment> appointments = appointmentService.getTodaysAppointments(doctor);
			logger.debug("appointments: {}", appointments);
			model.addAttribute("appointments", appointments);

			List<ActivePrescription> activePrescriptions = prescriptionService.getActivePrescriptions(doctor);
			logger.debug("activePrescriptions: {}", activePrescriptions);
			model.addAttribute("activeprescriptions", activePrescriptions);

		}

		return "doctordashboard";
	}

	@GetMapping("/viewdoctorschedule")
	public String viewDoctorSchedule(
			@RequestParam(value = RequestParamConstants.DOCTOR_ID, required = false) Long doctorID,
			@RequestParam(value = RequestParamConstants.USER_ID, required = false) Long userID,
			@RequestParam(value = RequestParamConstants.SCHEDULE_ID, required = false) Long scheduleID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading Doctor Schedule");

		Optional.ofNullable(doctorID).ifPresent(id -> logger.debug("Doctor ID: {}", id));
		Optional.ofNullable(userID).ifPresent(id -> logger.debug("User ID: {}", id));
		Optional.ofNullable(scheduleID).ifPresent(id -> logger.debug("Schedule ID: {}", id));

		try {
			DoctorScheduleWrapper doctorScheduleWrapper = doctorService.getDoctorScheduleWrapper(doctorID, scheduleID);
			model.addAttribute("doctorScheduleWrapper", doctorScheduleWrapper);
		} catch (NumberFormatException e) {
			logger.error("{}: {}", MessageConstants.SCHEDULE_ID_INVALID, e);
			model.addAttribute("errorMessage", MessageConstants.SCHEDULE_ID_INVALID);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.SCHEDULE_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.SCHEDULE_LOAD_ERROR);
		}

		model.addAttribute("doctorID", doctorID);
		model.addAttribute("userID", userID);
		model.addAttribute("scheduleID", scheduleID);
		model.addAttribute("source", source);

		return "viewdoctorschedule";
	}

	@PostMapping("/updatedoctorschedule")
	public String updateDoctorSchedules(@ModelAttribute DoctorScheduleWrapper doctorScheduleWrapper,
			@RequestParam(value = RequestParamConstants.DOCTOR_ID, required = false) Long doctorID,
			@RequestParam(value = RequestParamConstants.USER_ID, required = false) Long userID,
			@RequestParam(value = RequestParamConstants.SCHEDULE_ID, required = false) Long scheduleID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {

		logger.info("Updating Doctor Schedule");

		Optional.ofNullable(doctorID).ifPresent(id -> logger.debug("Doctor ID: {}", id));
		Optional.ofNullable(userID).ifPresent(id -> logger.debug("User ID: {}", id));
		Optional.ofNullable(scheduleID).ifPresent(id -> logger.debug("Schedule ID: {}", id));
		Optional.ofNullable(source).ifPresent(src -> logger.debug("Source: {}", src));

		try {
			doctorScheduleService.updateDoctorSchedules(doctorScheduleWrapper.getDoctorScheduleList(), doctorID,
					scheduleID);
			model.addAttribute("message", MessageConstants.SCHEDULE_UPDATE_SUCCESS);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.SCHEDULE_UPDATE_FAILURE, e);
			model.addAttribute("errorMessage", MessageConstants.SCHEDULE_UPDATE_FAILURE);
			model.addAttribute("doctorScheduleWrapper", doctorScheduleWrapper);
			return "viewdoctorschedule";
		}

		model.addAttribute("doctorScheduleWrapper", doctorScheduleWrapper);
		model.addAttribute("doctorID", doctorID);
		model.addAttribute("userID", userID);
		model.addAttribute("scheduleID", scheduleID);
		model.addAttribute("source", source);

		return "viewdoctorschedule";
	}

	@GetMapping("/createschedule")
	public String createSchedule(Model model,
			@RequestParam(value = RequestParamConstants.DOCTOR_ID, required = false) Long doctorID,
			@RequestParam(value = RequestParamConstants.AVAILABLE_DATE, required = false) LocalDate availableDate,
			@RequestParam(value = RequestParamConstants.START_TIME, required = false) LocalTime startTime,
			@RequestParam(value = RequestParamConstants.END_TIME, required = false) LocalTime endTime,
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source) {

		logger.info("Loading Create Schedule");

		if (logger.isDebugEnabled()) {
			logger.debug(
					"Request parameters - doctorID: {},  availableDate: {}, startTime: {}, endTime: {}, source: {}",
					doctorID, availableDate, startTime, endTime, source);
		}

		try {

			String scheduleStatus = SmartHealthCareConstants.APPROVED;
			doctorService.loadDoctorsAndFormValues(model, doctorID, availableDate, startTime, endTime, scheduleStatus);
			logger.debug("source: {}", source);
			model.addAttribute("source", source);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.CREATE_SCHEDULE_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.CREATE_SCHEDULE_LOAD_ERROR);
			return source; // load the page that called this request
		}

		return "createschedule";
	}

	@PostMapping("/saveschedule")
	public String saveDoctorSchedule(@RequestParam(RequestParamConstants.DOCTOR_ID) Long doctorID,
			@RequestParam(RequestParamConstants.AVAILABLE_DATE) String availableDate,
			@RequestParam(RequestParamConstants.START_TIME) String startTime,
			@RequestParam(RequestParamConstants.END_TIME) String endTime,
			@RequestParam(RequestParamConstants.AVAILABLE_COUNT) Long availableCount,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model,
			RedirectAttributes redirectAttributes, Authentication authentication) {

		logger.info("Creating Schedule for Doctor: {}", doctorID);

		if (logger.isDebugEnabled()) {
			logger.debug(
					"Request parameters - doctorID: {},  availableDate: {}, startTime: {}, endTime: {}, availableCount: {}, source: {}",
					doctorID, availableDate, startTime, endTime, availableCount, source);
		}

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		if (userDetails != null) {
			logger.debug("User Name: {}", userDetails.getUsername());
			logger.debug("User Authorities/Role: {}", userDetails.getAuthorities());
			model.addAttribute("roleName", userDetails.getAuthorities());
		}

		ScheduleStatus scheduleStatus = null;

		if (userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("ADMIN"))) {
			scheduleStatus = ScheduleStatus.APPROVED;
		} else if (userDetails.getAuthorities().stream()
				.anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("DOCTOR"))) {
			scheduleStatus = ScheduleStatus.PENDING;
		}

		try {

			DoctorScheduleRequest request = new DoctorScheduleRequest(doctorID, availableDate, startTime, endTime,
					availableCount, scheduleStatus);

			doctorScheduleService.createDoctorSchedule(request);

			if (userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("ADMIN"))) {
				model.addAttribute("message", MessageConstants.SCHEDULE_CREATE_SUCCESS);
			} else if (userDetails.getAuthorities().stream()
					.anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("DOCTOR"))) {
				model.addAttribute("message", MessageConstants.SCHEDULE_CREATE_PENDING);
			}

			List<Doctor> doctors = doctorService.getAllDoctors();
			model.addAttribute("doctors", doctors);
			model.addAttribute("source", source);
			return "createschedule";
		} catch (InvalidInputException | DoctorNotFoundException | OverlappingScheduleException e) {
			logger.error("{}: {}", MessageConstants.SCHEDULE_SAVE_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.SCHEDULE_SAVE_ERROR);

			model.addAttribute("doctorID", doctorID);
			model.addAttribute("availableDate", availableDate);
			model.addAttribute("startTime", startTime);
			model.addAttribute("endTime", endTime);
			model.addAttribute("scheduleStatus", scheduleStatus);
			model.addAttribute("doctors", doctorService.getAllDoctors());

			return "createschedule";
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.SCHEDULE_SAVE_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.SCHEDULE_SAVE_ERROR);

			return "createschedule";
		}
	}

	@GetMapping("/approveschedule")
	public String viewApproveSchedules(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading Approve Schedule");

		try {
			List<DoctorSchedule> doctorSchedules = doctorScheduleService.getPendingSchedules();
			model.addAttribute("doctorSchedules", doctorSchedules);
			model.addAttribute("source", source);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.APPROVE_SCHEDULE_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.APPROVE_SCHEDULE_LOAD_ERROR);
			return source;
		}

		return "approveschedule";
	}

	@PostMapping("/updateschedulestatus")
	public String updateScheduleStatus(@RequestParam(RequestParamConstants.SCHEDULE_ID) Long scheduleID,
			@RequestParam(RequestParamConstants.STATUS) ScheduleStatus scheduleStatus, Model model) {
		logger.info("Updating Schedule Status");

		try {
			Optional<DoctorSchedule> scheduleOptional = doctorScheduleService.findScheduleDetail(scheduleID);

			DoctorSchedule schedule = new DoctorSchedule();
			if (scheduleOptional.isPresent())
				schedule = scheduleOptional.get();
			schedule.setScheduleStatus(scheduleStatus);
			doctorScheduleService.saveDoctorSchedule(schedule);

			model.addAttribute("message", MessageConstants.SCHEDULE_STATUS_UPD_SUCCESS);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.SCHEDULE_STATUS_UPD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.SCHEDULE_STATUS_UPD_ERROR);
		}

		try {
			// Reload the list of doctor schedules
			List<DoctorSchedule> doctorSchedules = doctorScheduleService.getAllSchedules();
			model.addAttribute("doctorSchedules", doctorSchedules);
		} catch (Exception e) {

			logger.error("{}: {}", MessageConstants.SCHEDULE_RELOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.SCHEDULE_RELOAD_ERROR);
		}

		return "approveschedule";
	}

	@GetMapping("/viewallschedules")
	public String viewAllSchedules(@RequestParam(RequestParamConstants.DOCTOR_ID) Long doctorID,
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source, Model model) {
		logger.info("Loading All Schedules for Doctor ID: {}", doctorID);

		try {
			List<DoctorSchedule> doctorSchedules = doctorService.findDoctorSchedule(doctorID);
			model.addAttribute("doctorSchedules", doctorSchedules);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.VIEW_ALL_SCHEDULES_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.VIEW_ALL_SCHEDULES_LOAD_ERROR);
			return source;
		}

		model.addAttribute("doctorID", doctorID);
		model.addAttribute("source", source);

		return "viewallschedules";
	}

}