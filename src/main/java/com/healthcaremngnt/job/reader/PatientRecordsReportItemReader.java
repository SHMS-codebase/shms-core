package com.healthcaremngnt.job.reader;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.repository.AppointmentRepository;

@Component
@StepScope
public class PatientRecordsReportItemReader implements ItemReader<Appointment> {

	private static final Logger logger = LogManager.getLogger(PatientRecordsReportItemReader.class);

	@Autowired
	private AppointmentRepository appointmentRepository;

	private Iterator<Appointment> appointmentIterator;

	@Value("#{jobParameters['datePicker'] ?: '2025-01-01'}")
	private String dateString;

	@Value("#{jobParameters['reportType'] ?: 'daily'}")
	private String reportType;

	private boolean noAppointmentsAvailable = false;

	@Override
	public Appointment read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		logger.info("AppointmentItemReader::: read()");
		if (appointmentIterator == null) {

			try {
				LocalDate queryDate = parseDate(dateString, reportType);
				if (queryDate == null) {
					noAppointmentsAvailable = true; // Set the flag
					logger.error("Invalid date format: {}", dateString);
					return null; // Return null to indicate no appointments available
				}
				logger.debug("queryDate: {}", queryDate);

				List<Appointment> appointments = fetchAppointments(queryDate, reportType);
				logger.debug("appointments: {}", appointments);
				logger.debug("Read {} appointments total", appointments.size());

				if (appointments != null && !appointments.isEmpty()) {
					appointmentIterator = appointments.iterator();
				} else {
					noAppointmentsAvailable = true; // Set the flag
					appointmentIterator = List.<Appointment>of().iterator();
					logger.warn("No appointments found for the given date and report type.");
				}
			} catch (Exception e) {
				logger.error("Error in AppointmentItemReader: {}", e.getMessage());
				noAppointmentsAvailable = true; // Set the flag
				appointmentIterator = List.<Appointment>of().iterator();
			}

		}

		if (appointmentIterator.hasNext()) {
			return appointmentIterator.next();
		} else {
			return null; // No more appointments
		}
	}

	private LocalDate parseDate(String dateString, String reportType) {
		try {
			logger.info("Inside parseDate(): {}", dateString);
			if ("yearly".equalsIgnoreCase(reportType)) {
				int year = Integer.parseInt(dateString);
				return LocalDate.of(year, 1, 1); // Start of the year
			} else if ("monthly".equalsIgnoreCase(reportType) && dateString.matches("\\d{4}-\\d{2}")) {
				YearMonth yearMonth = YearMonth.parse(dateString);
				return yearMonth.atDay(1); // Start of the month
			} else if ("weekly".equalsIgnoreCase(reportType) && dateString.matches("\\d{4}-W\\d{2}")) {
				WeekFields weekFields = WeekFields.of(Locale.getDefault());
				int year = Integer.parseInt(dateString.substring(0, 4));
				int week = Integer.parseInt(dateString.substring(6));
				return LocalDate.ofYearDay(year, 1).with(weekFields.weekOfYear(), week);
			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				return LocalDate.parse(dateString, formatter);
			}
		} catch (DateTimeParseException | NumberFormatException e) {
			logger.error("Error parsing date: {}", e.getMessage());
			return null;
		}
	}

	private List<Appointment> fetchAppointments(LocalDate queryDate, String reportType) {
		switch (reportType.toLowerCase()) {
		case "daily":
			return appointmentRepository.findByAppointmentDate(queryDate);
		case "weekly":
			LocalDate startOfWeek = queryDate.minusDays(queryDate.getDayOfWeek().getValue() - 1);
			LocalDate endOfWeek = startOfWeek.plusDays(6);
			return appointmentRepository.findByAppointmentDateBetween(startOfWeek, endOfWeek);
		case "monthly":
			LocalDate startOfMonth = queryDate.withDayOfMonth(1);
			LocalDate endOfMonth = queryDate.withDayOfMonth(queryDate.lengthOfMonth());
			return appointmentRepository.findByAppointmentDateBetween(startOfMonth, endOfMonth);
		case "yearly":
			LocalDate startOfYear = queryDate.withDayOfYear(1);
			LocalDate endOfYear = queryDate.withDayOfYear(queryDate.lengthOfYear());
			return appointmentRepository.findByAppointmentDateBetween(startOfYear, endOfYear);
		default:
			throw new IllegalArgumentException("Invalid report type: " + reportType);
		}
	}

	public boolean isNoAppointmentsAvailable() {
		return noAppointmentsAvailable;
	}

}