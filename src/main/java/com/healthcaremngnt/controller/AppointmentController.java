package com.healthcaremngnt.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.AppointmentRequest;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.DoctorScheduleService;
import com.healthcaremngnt.service.DoctorService;
import com.healthcaremngnt.service.PatientService;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

	private static final Logger logger = LogManager.getLogger(AppointmentController.class);

	private final DoctorService doctorService;
	private final PatientService patientService;
	private final DoctorScheduleService doctorScheduleService;
	private final AppointmentService appointmentService;

	public AppointmentController(DoctorService doctorService, PatientService patientService,
			DoctorScheduleService doctorScheduleService, AppointmentService appointmentService) {
		this.doctorService = doctorService;
		this.patientService = patientService;
		this.doctorScheduleService = doctorScheduleService;
		this.appointmentService = appointmentService;
	}

	@GetMapping("/createappointment")
	public String showCreateAppointment(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading Create Appointment");
		populateModelWithDoctorsAndPatients(model);
		model.addAttribute("source", source);
		return "createappointment";
	}

	@GetMapping("/available-dates")
	@ResponseBody
	public List<LocalDate> getAvailableDates(@RequestParam Long doctorID) {
		logger.info("Loading Available Date for doctorID: {}", doctorID);
		return doctorScheduleService.getAvailableDatesByDoctorID(doctorID);
	}

	@GetMapping("/available-time-slots")
	@ResponseBody
	public List<String> getAvailableTimeSlots(@RequestParam Long doctorID, @RequestParam String date) {
		logger.info("Loading Available Time Slots for doctorID: {} and availableDate: {}", doctorID, date);
		return doctorScheduleService.getAvailableTimeSlotsByDoctorIDAndDate(doctorID, LocalDate.parse(date));
	}

	@PostMapping("/bookAppointment")
	public String bookAppointment(AppointmentRequest appointmentRequest,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Booking Appointment: {}", appointmentRequest);
		try {
			appointmentService.bookAppointment(appointmentRequest);
			logger.debug("{}", MessageConstants.APMNT_BOOKING_SUCCESS);
			model.addAttribute("message", MessageConstants.APMNT_BOOKING_SUCCESS);
			populateModelWithDoctorsAndPatients(model);
			model.addAttribute("source", source);
			return "createappointment";
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.APMNT_BOOKING_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.APMNT_BOOKING_ERROR);
			return "createappointment";
		}
	}

	private void populateModelWithDoctorsAndPatients(Model model) {
		logger.info("Populating Model With Doctors And Patients");
		List<Doctor> doctors = doctorService.getDoctorsWithSchedule();
		List<Patient> patients = patientService.getAllPatients();
		model.addAttribute("doctors", doctors);
		model.addAttribute("patients", patients);
	}

	@GetMapping("/viewappointment")
	public String viewAppointment(
			@RequestParam(value = RequestParamConstants.APPOINTMENTID, required = false) Long appointmentID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading View Appointment");
		Optional.ofNullable(appointmentID).ifPresent(id -> logger.debug("Appointment ID: {}", id));
		// or
		Optional.ofNullable(appointmentID).ifPresent(id -> {
			String maskedID = String.valueOf(id).replaceAll("(?<=.{2}).(?=.*$)", "*");
			logger.debug("Appointment ID: {}", maskedID);
		});

		try {
			Optional<Appointment> appointmentOptional = appointmentService.getAppointmentDetails(appointmentID);
			Appointment appointment = appointmentOptional.orElseGet(Appointment::new);

			logger.debug("appointment: {}", appointment);
			model.addAttribute("appointment", appointment);
		} catch (NumberFormatException e) {
			logger.error("{}: {}", MessageConstants.APMNT_INVALID_IDFORMAT, appointmentID, e);
			model.addAttribute("errorMessage", MessageConstants.APMNT_INVALID_IDFORMAT);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.APMNT_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.APMNT_LOAD_ERROR);
		}

		model.addAttribute("appointmentID", appointmentID);
		model.addAttribute("source", source);

		return "viewappointment";
	}

	@PostMapping("/updateappointment")
	public String updateAppointment(@ModelAttribute Appointment appointment,
			@RequestParam(value = RequestParamConstants.APPOINTMENTID, required = false) Long appointmentID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Updating Appointments");
		Optional.ofNullable(appointment).ifPresent(id -> logger.debug("appointment: {}", id));
		Optional.ofNullable(appointmentID).ifPresent(id -> logger.debug("Appointment ID", id));
		Optional.ofNullable(source).ifPresent(src -> logger.debug("Source: {}", src));

		try {
			appointmentService.updateAppointmentDetails(appointment);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.APMNT_UPDATE_FAILURE, e);
			model.addAttribute("errorMessage", MessageConstants.APMNT_UPDATE_FAILURE);
			model.addAttribute("appointment", appointment);
			return "viewappointment";
		}

		logger.debug("{}", MessageConstants.APMNT_UPDATE_SUCCESS);

		model.addAttribute("appointment", appointment);
		model.addAttribute("appointmentID", appointmentID);
		model.addAttribute("message", MessageConstants.APMNT_UPDATE_SUCCESS);
		model.addAttribute("source", source);

		return "viewappointment";
	}

}