package com.healthcaremngnt.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.api.client.util.DateTime;
import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;
import com.healthcaremngnt.exceptions.DoctorNotFoundException;
import com.healthcaremngnt.exceptions.PatientNotFoundException;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.AppointmentRequest;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.Treatment;
import com.healthcaremngnt.model.UpcomingAppointment;
import com.healthcaremngnt.service.AppointmentService;
import com.healthcaremngnt.service.CalendarIntegrationService;
import com.healthcaremngnt.service.DoctorScheduleService;
import com.healthcaremngnt.service.DoctorService;
import com.healthcaremngnt.service.PatientService;
import com.healthcaremngnt.service.TreatmentService;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

	private static final Logger logger = LogManager.getLogger(AppointmentController.class);

	private final DoctorService doctorService;
	private final PatientService patientService;
	private final DoctorScheduleService doctorScheduleService;
	private final AppointmentService appointmentService;
	private final TreatmentService treatmentService;

	@Autowired
	private CalendarIntegrationService calendarIntegrationService;

	public AppointmentController(DoctorService doctorService, PatientService patientService,
			DoctorScheduleService doctorScheduleService, AppointmentService appointmentService,
			TreatmentService treatmentService) {
		this.doctorService = doctorService;
		this.patientService = patientService;
		this.doctorScheduleService = doctorScheduleService;
		this.appointmentService = appointmentService;
		this.treatmentService = treatmentService;
	}

	@GetMapping("/createappointment")
	public String viewCreateAppointment(
			@RequestParam(value = RequestParamConstants.TREATMENT_ID, required = false) Long treatmentID,
			@RequestParam(value = RequestParamConstants.DOCTOR_ID, required = false) Long doctorID,
			@RequestParam(value = RequestParamConstants.PATIENT_ID, required = false) Long patientID,
			@RequestParam(value = RequestParamConstants.PARENT_APPOINTMENT_ID, required = false) Long parentAppointmentID,
			@RequestParam(value = RequestParamConstants.IS_FOLLOWUP, required = false) Boolean isFollowup,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model)
			throws DoctorNotFoundException, PatientNotFoundException {
		logger.info("Loading Create Appointment");

		Optional.ofNullable(treatmentID).ifPresent(id -> logger.debug("treatmentID : {}", id));
		Optional.ofNullable(doctorID).ifPresent(id -> logger.debug("doctorID : {}", id));
		Optional.ofNullable(patientID).ifPresent(id -> logger.debug("patientID : {}", id));
		Optional.ofNullable(parentAppointmentID).ifPresent(id -> logger.debug("parentAppointmentID : {}", id));
		Optional.ofNullable(isFollowup).ifPresent(id -> logger.debug("isFollowup : {}", id));

		if (treatmentID != null) {
			populateDoctorAndPatientDetails(model, doctorID, patientID);
			model.addAttribute("isFollowup", true);
			model.addAttribute("parentAppointmentID", parentAppointmentID);

		} else {
			populateDoctorsAndPatients(model);
			model.addAttribute("isFollowup", false);
		}

		model.addAttribute("source", source);
		return "createappointment";
	}

	private void populateDoctorAndPatientDetails(Model model, Long doctorID, Long patientID)
			throws DoctorNotFoundException, PatientNotFoundException {

		logger.info("Populating Model With Doctor And Patient details of that treatment!");

		if (doctorID != null) {
			Doctor doctor = doctorService.getDoctorDetails(doctorID);
			model.addAttribute("doctor", doctor);
		}

		if (patientID != null) {
			Patient patient = patientService.getPatientDetails(patientID);
			model.addAttribute("patient", patient);
		}
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
			@RequestParam(RequestParamConstants.SOURCE) String source,
			@RequestParam(value = RequestParamConstants.TREATMENT_ID, required = false) Long treatmentID,
			@RequestParam(value = RequestParamConstants.PARENT_APPOINTMENT_ID, required = false) Long parentAppointmentID,
			@RequestParam(value = RequestParamConstants.IS_FOLLOWUP, required = false) Boolean isFollowup,
			Model model) {
		logger.info("Booking Appointment: {}", appointmentRequest);

		Optional.ofNullable(treatmentID).ifPresent(id -> logger.debug("treatmentID : {}", id));
		Optional.ofNullable(parentAppointmentID).ifPresent(id -> logger.debug("parentAppointmentID : {}", id));
		Optional.ofNullable(isFollowup).ifPresent(id -> logger.debug("isFollowup : {}", id));

		model.addAttribute("source", source);

		try {

			boolean followupFlag = (isFollowup != null) ? isFollowup : false;

			Appointment appointment = appointmentService.bookAppointment(appointmentRequest, treatmentID,
					parentAppointmentID, followupFlag);

			// Integrate with Google Calendar
			try {
				LocalDateTime start = LocalDateTime.of(appointment.getAppointmentDate(),
						appointment.getAppointmentTime());
				LocalDateTime end = start.plusMinutes(30); // default duration

				ZonedDateTime zonedStart = start.atZone(ZoneId.of("Asia/Kolkata"));
				ZonedDateTime zonedEnd = end.atZone(ZoneId.of("Asia/Kolkata"));

				DateTime googleStart = new DateTime(zonedStart.toInstant().toEpochMilli());
				DateTime googleEnd = new DateTime(zonedEnd.toInstant().toEpochMilli());

				String eventId = calendarIntegrationService.createAppointmentEvent(googleStart, googleEnd,
						"Appointment for " + appointment.getPatient().getPatientName());

				appointment.setCalendarEventId(eventId);
				logger.info("Google Calendar event created with ID: {}", eventId);

			} catch (IOException calendarEx) {
				logger.warn("Failed to create calendar event: {}", calendarEx.getMessage());
			}

			logger.debug("{}", MessageConstants.APMNT_BOOKING_SUCCESS);
			model.addAttribute("message", MessageConstants.APMNT_BOOKING_SUCCESS);
			populateDoctorsAndPatients(model);

			// Call async method
			CompletableFuture.runAsync(() -> appointmentService.sendAppointmentEmail(appointment));

			return "createappointment";
		} catch (JpaSystemException e) {
			logger.error("JPA System Exception during appointment booking: ", e);
			// Log the root cause
			Throwable rootCause = e.getRootCause();
			if (rootCause != null) {
				logger.error("Root cause: {}", rootCause.getMessage(), rootCause);
			}
			model.addAttribute("errorMessage", "Database error occurred. Please check your input and try again.");
			populateDoctorsAndPatients(model);
			return "createappointment";

		} catch (DataIntegrityViolationException e) {
			logger.error("Data integrity violation: ", e);
			model.addAttribute("errorMessage", "Appointment conflict detected. Please choose a different time slot.");
			populateDoctorsAndPatients(model);
			return "createappointment";

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.APMNT_BOOKING_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.APMNT_BOOKING_ERROR);
			populateDoctorsAndPatients(model);
			return "createappointment";
		}
	}

	private void populateDoctorsAndPatients(Model model) {
		logger.info("Populating Model With Doctors And Patients");
		List<Doctor> doctors = doctorService.getDoctorsWithSchedule();
		List<Patient> patients = patientService.getAllPatients();
		model.addAttribute("doctors", doctors);
		model.addAttribute("patients", patients);
	}

	@GetMapping("/viewappointment")
	public String viewAppointment(
			@RequestParam(value = RequestParamConstants.APPOINTMENT_ID, required = false) Long appointmentID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Loading View Appointment");
		Optional.ofNullable(appointmentID).ifPresent(id -> logger.debug("Appointment ID: {}", id));
		// or
		Optional.ofNullable(appointmentID).ifPresent(id -> {
			String maskedID = String.valueOf(id).replaceAll("(?<=.{2}).(?=.*$)", "*");
			logger.debug("Appointment ID: {}", maskedID);
		});

		try {
			Appointment appointment = appointmentService.getAppointmentDetails(appointmentID);

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
			@RequestParam(value = RequestParamConstants.APPOINTMENT_ID, required = false) Long appointmentID,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Updating Appointments");
		Optional.ofNullable(appointment).ifPresent(id -> logger.debug("appointment: {}", id));
		Optional.ofNullable(appointmentID).ifPresent(id -> logger.debug("Appointment ID", id));
		Optional.ofNullable(source).ifPresent(src -> logger.debug("Source: {}", src));

		try {
			Appointment updatedAppointment = appointmentService.updateAppointmentDetails(appointment);
			logger.debug("updatedAppointment: {}", updatedAppointment);

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

	@GetMapping("/followupappointments")
	public String viewFollowupAppointments(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {

		logger.info("Loading all Follow-Up Appointments for booking Follow-Up Appointment!!");

		try {
			List<Treatment> followupAppointments = treatmentService.getFollowUpTreatments();
			logger.debug("followupAppointments: {}", followupAppointments);
			model.addAttribute("followupAppointments", followupAppointments);
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.FOLLOWUP_APMNT_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.FOLLOWUP_APMNT_LOAD_ERROR);
			return source;
		}

		model.addAttribute("source", source);

		return "followupappointments";
	}

	@GetMapping("/cancelappointments")
	public String viewCancelAppointments(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {

		logger.info("Loading Cancel Appointments Page for cancellation with not eligible ones marked!!");

		try {

			LocalDate today = LocalDate.now();
			LocalDate nextWeekDay = today.plusDays(7);

			// Load the upcoming appointments from today till next week i.e., 7 days on load
			List<UpcomingAppointment> upcomingAppointments = appointmentService
					.getUpcomingAppointmentBetween(nextWeekDay, today);
			logger.debug("upcomingAppointments: {}", upcomingAppointments);
			model.addAttribute("upcomingAppointments", upcomingAppointments);

			List<Doctor> doctors = doctorService.getAllDoctors();
			model.addAttribute("doctors", doctors);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.CANCEL_APMNT_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.CANCEL_APMNT_LOAD_ERROR);
			return source;
		}

		model.addAttribute("source", source);

		return "cancelappointments";
	}

	@GetMapping("/fetchappointments")
	public String FetchCancelAppointments(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {

		logger.info("Loading all Appointments for cancellation and mark the ones not eligible too!!");

		try {

			List<Appointment> upcomingAppointments = appointmentService.getUpcomingAppointments();
			logger.debug("upcomingAppointments: {}", upcomingAppointments);
			model.addAttribute("upcomingAppointments", upcomingAppointments);

			List<Doctor> doctors = doctorService.getAllDoctors();
			model.addAttribute("doctors", doctors);

		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.CANCEL_APMNT_LOAD_ERROR, e);
			model.addAttribute("errorMessage", MessageConstants.CANCEL_APMNT_LOAD_ERROR);
			return source;
		}

		model.addAttribute("source", source);

		return "cancelappointments";
	}

}