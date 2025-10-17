package com.healthcaremngnt.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import com.healthcaremngnt.model.BreadcrumbItem;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.DoctorSchedule;
import com.healthcaremngnt.model.DoctorScheduleRequest;
import com.healthcaremngnt.model.DoctorScheduleWrapper;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.DoctorScheduleService;
import com.healthcaremngnt.service.DoctorService;
import com.healthcaremngnt.service.PrescriptionService;

@Controller
@RequestMapping("/api/v1/doctor")
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
	public String getDoctorDashboard(@SessionAttribute(RequestParamConstants.USER_NAME) String userName, Model model)
			throws DoctorNotFoundException {
		logger.info("Loading Doctor Dashboard");
		Doctor doctor = doctorService.getDoctorInfoCard(userName);

		logger.debug("Doctor Details: {}", doctor);
		model.addAttribute("doctor", doctor);

		List<Appointment> appointments = appointmentService.getTodaysAppointments(doctor);
		logger.debug("appointments: {}", appointments);
		model.addAttribute("appointments", appointments);

		List<ActivePrescription> activePrescriptions = prescriptionService.getActivePrescriptions(doctor);
		logger.debug("activePrescriptions: {}", activePrescriptions);
		model.addAttribute("activeprescriptions", activePrescriptions);

		return "doctordashboard";

	}

	@GetMapping("/viewdoctorschedule")
	public String viewDoctorSchedule(
			@RequestParam(value = RequestParamConstants.DOCTOR_ID, required = false) Long doctorID,
			@RequestParam(value = RequestParamConstants.USER_ID, required = false) Long userID,
			@RequestParam(value = RequestParamConstants.SCHEDULE_ID, required = false) Long scheduleID,
			@RequestParam(RequestParamConstants.SOURCE) String source,
			@RequestParam(value = RequestParamConstants.FLOW, required = false) String flow, Model model) {
		logger.info("Loading Doctor Schedule");

		Optional.ofNullable(doctorID).ifPresent(id -> logger.debug("Doctor ID: {}", id));
		Optional.ofNullable(userID).ifPresent(id -> logger.debug("User ID: {}", id));
		Optional.ofNullable(scheduleID).ifPresent(id -> logger.debug("Schedule ID: {}", id));
		Optional.ofNullable(source).ifPresent(id -> logger.debug("Source: {}", id));

		try {
			DoctorScheduleWrapper doctorScheduleWrapper = doctorService.getDoctorScheduleWrapper(doctorID, scheduleID);
			logger.debug("doctorScheduleWrapper: {}", doctorScheduleWrapper);
			model.addAttribute("doctorScheduleWrapper", doctorScheduleWrapper);
		} catch (NumberFormatException e) {
			logger.error("{}: {}", MessageConstants.SCHEDULE_ID_INVALID, e.getMessage());
			model.addAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.SCHEDULE_LOAD_ERROR, e.getMessage());
			model.addAttribute("errorMessage", e.getMessage());
		}

		List<BreadcrumbItem> breadcrumbTrail = new ArrayList<>();
		breadcrumbTrail.add(new BreadcrumbItem("Dashboard", "/api/v1/auth/dashboard/admin"));

		if (flow.equalsIgnoreCase("searchschedule")) {
			breadcrumbTrail.add(new BreadcrumbItem("Search Schedules", "/api/v1/search/searchschedule"));
		} else if (!flow.equalsIgnoreCase("searchschedule")) {

			// Need to check the flow before this page is called

			breadcrumbTrail
					.add(new BreadcrumbItem("All Schedules", "/api/v1/doctor/viewallschedules?doctorID=" + doctorID));
		}

		breadcrumbTrail.add(new BreadcrumbItem("View/Update Schedule", null));

		model.addAttribute("breadcrumbTrail", breadcrumbTrail);

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
			logger.error("{}: {}", MessageConstants.SCHEDULE_UPDATE_FAILURE, e.getMessage());
			model.addAttribute("errorMessage", e.getMessage());
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

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.CREATE_SCHEDULE_LOAD_ERROR, e.getMessage());
			model.addAttribute("errorMessage", e.getMessage());
			return source; // load the page that called this request
		}

		model.addAttribute("source", source);
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

		// Using Switch Expression to determine schedule status based on user role
		scheduleStatus = switch (getUserRole(userDetails)) {
		case "ROLE_ADMIN" -> ScheduleStatus.APPROVED;
		case "ROLE_DOCTOR" -> ScheduleStatus.PENDING;
		default -> scheduleStatus;
		};

		model.addAttribute("source", source);

		logger.debug("Determined Schedule Status: {}", scheduleStatus);
		logger.debug("Initial Doctor ID in Model: {}", model.getAttribute("doctorID"));
		if (scheduleStatus.equals(ScheduleStatus.PENDING)) {
			model.addAttribute("doctorID", doctorID);
		} else {
			model.addAttribute("doctorID", null);
		}
		logger.debug("Final Doctor ID in Model: {}", model.getAttribute("doctorID"));

		try {

			DoctorScheduleRequest request = new DoctorScheduleRequest(doctorID, availableDate, startTime, endTime,
					availableCount, scheduleStatus);

			doctorScheduleService.createDoctorSchedule(request);

			if (userDetails.getAuthorities().stream()
					.anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("ROLE_ADMIN"))) {
				model.addAttribute("message", MessageConstants.SCHEDULE_CREATE_SUCCESS);
			} else if (userDetails.getAuthorities().stream()
					.anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("ROLE_DOCTOR"))) {
				model.addAttribute("message", MessageConstants.SCHEDULE_CREATE_PENDING);
			}

			List<Doctor> doctors = doctorService.getAllDoctors();
			model.addAttribute("doctors", doctors);

			return "createschedule";
		} catch (InvalidInputException | DoctorNotFoundException | OverlappingScheduleException e) {
			logger.error("{}: {}", MessageConstants.SCHEDULE_SAVE_ERROR, e.getMessage());
			model.addAttribute("errorMessage", e.getMessage());

//			model.addAttribute("doctorID", doctorID);
			model.addAttribute("availableDate", availableDate);
			model.addAttribute("startTime", startTime);
			model.addAttribute("endTime", endTime);
			model.addAttribute("scheduleStatus", scheduleStatus);
			model.addAttribute("doctors", doctorService.getAllDoctors());

			return "createschedule";
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.SCHEDULE_SAVE_ERROR, e.getMessage());
			model.addAttribute("errorMessage", e.getMessage());

			return "createschedule";
		}
	}

	private String getUserRole(UserDetails userDetails) {

		logger.debug("Determining user role from authorities: {}", userDetails.getAuthorities());
		return userDetails.getAuthorities().stream().map(auth -> auth.getAuthority().toUpperCase())
				.filter(role -> Set.of("ROLE_ADMIN", "ROLE_DOCTOR").contains(role)).findFirst().orElse("UNKNOWN");
	}

	@GetMapping("/approveschedule")
	public String viewApproveSchedules(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading Approve Schedule");

		model.addAttribute("source", source);

		try {
			List<DoctorSchedule> doctorSchedules = doctorScheduleService.getPendingSchedules();
			model.addAttribute("doctorSchedules", doctorSchedules);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.APPROVE_SCHEDULE_LOAD_ERROR, e.getMessage());
			model.addAttribute("errorMessage", e.getMessage());
			return source;
		}

		return "approveschedule";
	}

	@PostMapping("/updateschedulestatus")
	public String updateScheduleStatus(@RequestParam(RequestParamConstants.SCHEDULE_ID) Long scheduleID,
			@RequestParam(RequestParamConstants.STATUS) ScheduleStatus scheduleStatus, Model model) {
		logger.info("Updating Schedule Status");

		try {
			DoctorSchedule schedule = doctorScheduleService.findScheduleDetail(scheduleID);

			schedule.setScheduleStatus(scheduleStatus);
			doctorScheduleService.saveDoctorSchedule(schedule);

			model.addAttribute("message", MessageConstants.SCHEDULE_STATUS_UPD_SUCCESS);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.SCHEDULE_STATUS_UPD_ERROR, e.getMessage());
			model.addAttribute("errorMessage", e.getMessage());
		}

		try {
			// Reload the list of doctor schedules
			List<DoctorSchedule> doctorSchedules = doctorScheduleService.getAllSchedules();
			model.addAttribute("doctorSchedules", doctorSchedules);
		} catch (Exception e) {

			logger.error("{}: {}", MessageConstants.SCHEDULE_RELOAD_ERROR, e.getMessage());
			model.addAttribute("errorMessage", e.getMessage());
		}

		return "approveschedule";
	}

	@GetMapping("/viewallschedules")
	public String viewAllSchedules(@RequestParam(RequestParamConstants.DOCTOR_ID) Long doctorID,
			@RequestParam(value = RequestParamConstants.SOURCE, required = false) String source,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, Model model) {
		logger.info("Loading All Schedules for Doctor ID: {}, Page: {}", doctorID, page);

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("availableDate").descending());
			Page<DoctorSchedule> doctorSchedulesPage = doctorService.findDoctorSchedule(doctorID, pageable);

			model.addAttribute("doctorSchedules", doctorSchedulesPage.getContent());
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", doctorSchedulesPage.getTotalPages());
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.VIEW_ALL_SCHEDULES_LOAD_ERROR, e.getMessage());
			model.addAttribute("errorMessage", e.getMessage());
			return source;
		}

		model.addAttribute("doctorID", doctorID);
		model.addAttribute("source", source);

		return "viewallschedules";
	}

}