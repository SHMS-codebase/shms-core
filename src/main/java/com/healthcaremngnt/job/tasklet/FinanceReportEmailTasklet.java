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
public class FinanceReportEmailTasklet implements Tasklet {

	private static final Logger logger = LogManager.getLogger(FinanceReportEmailTasklet.class);

	@Autowired
	private EmailService emailService;

	@Value("${spring.mail.username}")
	private String recipient; // Inject the recipient email from application.properties

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		logger.info("FinanceReportEmailTasklet::: execute()");

		JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
		String reportGenerated = jobExecution.getExecutionContext().getString("reportGenerated");

		if (Boolean.parseBoolean(reportGenerated)) {
			String reportFormat = jobExecution.getJobParameters().getString("reportFormat");
			String formattedNow = jobExecution.getJobParameters().getString("formattedNow");
			String reportFilePath = getReportFilePath(reportFormat, formattedNow);

			logger.debug("reportGenerated: {}", reportGenerated);
			logger.debug("reportFormat: {}", reportFormat);
			logger.debug("formattedNow: {}", formattedNow);
			logger.debug("reportFilePath: {}", reportFilePath);

			emailService.sendAppointmentEmail(reportFilePath, recipient);
		}
		return RepeatStatus.FINISHED;
	}

	private String getReportFilePath(String reportFormat, String formattedNow) {

		logger.info("FinanceReportEmailTasklet::: getReportFilePath()");

		String baseFilePath = "reports/finances/finance_report_" + formattedNow;
		switch (reportFormat) {
		case "pdf":
			return baseFilePath + ".pdf";
		case "word":
			return baseFilePath + ".docx";
		case "excel":
			return baseFilePath + ".xlsx";
		default:
			return baseFilePath + ".txt";
		}
	}

}