package com.healthcaremngnt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.healthcaremngnt.config", "com.healthcaremngnt.repository", "com.healthcaremngnt"})
@EnableTransactionManagement
public class BatchJobRunnerApplication {

	private static final Logger logger = LogManager.getLogger(BatchJobRunnerApplication.class);

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job deleteExpiredSchedulesJob;

	public static void main(String[] args) {
		SpringApplication.run(BatchJobRunnerApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
		return args -> {
			try {
				logger.info("Running the Job for testing");
				JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
						.toJobParameters();
				JobExecution jobExecution = jobLauncher.run(deleteExpiredSchedulesJob, jobParameters);
				logger.info("Job Execution Status: " + jobExecution.getStatus());
			} catch (Exception e) {
				logger.error("Failed to execute job", e);
			}
		};
	}

}