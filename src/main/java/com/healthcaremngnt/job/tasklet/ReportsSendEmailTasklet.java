package com.healthcaremngnt.job.tasklet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.healthcaremngnt.service.EmailService;

@Component
@StepScope
public class ReportsSendEmailTasklet implements Tasklet {

	private static final Logger logger = LogManager.getLogger(ReportsSendEmailTasklet.class);

	@Autowired
	private EmailService emailService;

	@Value("${spring.mail.username}")
	private String recipient; // Inject the recipient email from application.properties

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		logger.info("ReportsSendEmailTasklet::: execute()");

		
		JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
		String jobName = jobExecution.getJobInstance().getJobName();
		logger.debug("Job Name: {}", jobName);

		String reportGenerated = jobExecution.getExecutionContext().getString("reportGenerated");

		if (Boolean.parseBoolean(reportGenerated)) {
			String reportFormat = jobExecution.getJobParameters().getString("reportFormat");
			String formattedNow = jobExecution.getJobParameters().getString("formattedNow");

			// Send Job Name as well!!
			String reportFilePath = getReportFilePath(jobName, reportFormat, formattedNow);

			logger.debug("reportGenerated: {}", reportGenerated);
			logger.debug("reportFormat: {}", reportFormat);
			logger.debug("formattedNow: {}", formattedNow);
			logger.debug("reportFilePath: {}", reportFilePath);

			emailService.sendReportsEmail(jobName, reportFilePath, recipient);
		}
		return RepeatStatus.FINISHED;
	}

	private String getReportFilePath(String jobName, String reportFormat, String formattedNow) {

		logger.info("ReportsSendEmailTasklet::: getReportFilePath()");

		String baseFilePath = "";

		if (jobName.equalsIgnoreCase("appointmentReportJob"))
			baseFilePath = "reports/appointment/appointment_report_" + formattedNow;
		else if (jobName.equalsIgnoreCase("patientReportJob"))
			baseFilePath = "reports/patient/patient_report_" + formattedNow;
		else if (jobName.equalsIgnoreCase("billingReportJob"))
			baseFilePath = "reports/billing/billing_report_" + formattedNow;

		return switch (reportFormat.toLowerCase()) {
		case "pdf" -> baseFilePath + ".pdf";
		case "word" -> baseFilePath + ".docx";
		case "excel" -> baseFilePath + ".xlsx";
		default -> baseFilePath + ".txt";
		};
	}

}