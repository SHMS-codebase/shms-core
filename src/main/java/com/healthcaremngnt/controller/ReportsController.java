package com.healthcaremngnt.controller;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.constants.RequestParamConstants;

@Controller
@RequestMapping("/reports")
public class ReportsController {

	private static final Logger logger = LogManager.getLogger(ReportsController.class);

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job appointmentReportJob;

	@GetMapping("/appointmentreport")
	public String viewAppointmentReport(@RequestParam(RequestParamConstants.SOURCE) String source, Model model) {
		logger.info("Viewing Appointment Report!!!");
		model.addAttribute("source", source);
		return "appointmentreport";
	}

	@GetMapping("/patientreport")
	public String viewPatientReport() {
		logger.info("Viewing Patient Report!!!");
		return "patientreport";
	}

	@PostMapping("/appointmentreport")
	public ResponseEntity<?> generateReport(@RequestParam(RequestParamConstants.REPORT_TYPE) String reportType,
			@RequestParam(RequestParamConstants.DATE_PICKER) String datePicker,
			@RequestParam(RequestParamConstants.REPORT_FORMAT) String reportFormat,
			@RequestParam(RequestParamConstants.SOURCE) String source, Model model) throws Exception {
		logger.info("Generating Appointment Report!!!");

		try {

			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
			String formattedNow = now.format(formatter);

			JobParameters jobParams = new JobParametersBuilder().addString("reportType", reportType)
					.addString("datePicker", datePicker).addString("reportFormat", reportFormat)
					.addString("formattedNow", formattedNow).addLong("Start-At", System.currentTimeMillis())
					.toJobParameters();

			logger.debug("reportType: {}", reportType);
			logger.debug("datePicker: {}", datePicker);
			logger.debug("reportFormat: {}", reportFormat);

			JobExecution jobExecution = jobLauncher.run(appointmentReportJob, jobParams);

			// Wait for the job to complete
			while (jobExecution.isRunning()) {
				Thread.sleep(1000);
			}

			// Check job status
			if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
				logger.error("{}", MessageConstants.JOB_FAILURE);
				model.addAttribute("errorMessage", MessageConstants.JOB_FAILURE);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_HTML)
						.body("error");
			}

			String reportGeneratedStr = jobExecution.getExecutionContext().getString("reportGenerated", "false");
			boolean reportGenerated = Boolean.parseBoolean(reportGeneratedStr);

			if (!reportGenerated) {
				logger.error("{}", MessageConstants.NO_APMNTS_REPORT_ERROR);
				model.addAttribute("errorMessage", MessageConstants.NO_APMNTS_REPORT_ERROR);
				return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(model);
			}

			String reportFilePath = getReportFilePath(reportFormat, formattedNow);
			logger.debug("reportFilePath: {}", reportFilePath);

			File reportFile = new File(reportFilePath);
			logger.debug("reportFile: {}", reportFile);

			// Retry logic
			int retryCount = 0;
			int maxRetries = 5;
			while (!reportFile.exists() || reportFile.length() == 0 && retryCount < maxRetries) {
				Thread.sleep(1000);
				retryCount++;
				reportFile = new File(reportFilePath);
			}

			if (!reportFile.exists()) {
				logger.error("{}: {}", MessageConstants.REPORT_FILE_NOT_FOUND, reportFilePath);
				model.addAttribute("errorMessage", MessageConstants.REPORT_FILE_NOT_FOUND);
				return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(model);
			}

			model.addAttribute("source", source);

			try (FileInputStream fis = new FileInputStream(reportFile)) {
				InputStreamResource resource = new InputStreamResource(new FileInputStream(reportFile));
				MediaType mediaType = getMediaType(reportFormat);
				logger.debug("{}", MessageConstants.APMNT_REPORT_GENERATED);
				model.addAttribute("message", MessageConstants.APMNT_REPORT_GENERATED);
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + reportFile.getName())
						.contentType(mediaType).contentLength(reportFile.length()).body(resource);
			}
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.APMNT_REPORT_FAILURE, e);
			model.addAttribute("errorMessage", MessageConstants.APMNT_REPORT_FAILURE);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_HTML).body(model);
		}
	}

	private String getReportFilePath(String reportFormat, String formattedNow) {
		String baseFilePath = "reports/appointment_report_" + formattedNow;
		return switch (reportFormat.toLowerCase()) {
		case "pdf" -> baseFilePath + ".pdf";
		case "word" -> baseFilePath + ".docx";
		case "excel" -> baseFilePath + ".xlsx";
		default -> baseFilePath + ".txt";
		};
	}

	private MediaType getMediaType(String reportFormat) {
		return switch (reportFormat.toLowerCase()) {
		case "pdf" -> MediaType.APPLICATION_PDF;
		case "word", "excel" -> MediaType.APPLICATION_OCTET_STREAM;
		default -> MediaType.TEXT_PLAIN;
		};
	}

}