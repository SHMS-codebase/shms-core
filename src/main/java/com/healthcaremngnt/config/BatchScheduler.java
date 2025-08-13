package com.healthcaremngnt.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchScheduler {

	private static final Logger logger = LogManager.getLogger(BatchScheduler.class);

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job deleteExpiredSchedulesJob;

	@Autowired
	private Job appointmentNoShowJob;

	@Autowired
	private Job archivePatientRecordsJob;

	@Autowired
	private Job deletePatientRecordsJob;

//	@Autowired
//	private Job appointmentReminderJob;

	// @Scheduled(fixedRate = 60000) // Runs every minute for testing purposes
	@Scheduled(cron = "0 0 0 * * ?") // actual working!!!!!
	public void performDeleteExpiredSchedulesJob() {
		logger.info("BatchScheduler - performDeleteExpiredSchedulesJob");
		JobParameters params = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
		try {
			jobLauncher.run(deleteExpiredSchedulesJob, params);
		} catch (Exception e) {
			logger.error("Error executing deleteExpiredSchedulesJob", e);
		}
	}

	@Scheduled(cron = "0 0 12,14,16,18,20,21 * * ?", zone = "Asia/Kolkata")
	public void performAppointmentNoShowJob() {
		logger.info("BatchScheduler - performAppointmentNoShowJob");
		JobParameters params = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
		try {
			jobLauncher.run(appointmentNoShowJob, params);
		} catch (Exception e) {
			logger.error("Error executing performAppointmentNoShowJob", e);
		}
	}

	@Scheduled(cron = "0 0 0 1 1,7 *", zone = "Asia/Kolkata") // Every 6 months
	public void performArchivePatientRecordsJob() {
		logger.info("BatchScheduler - performArchivePatientRecordsJob");
		JobParameters params = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
		try {
			jobLauncher.run(archivePatientRecordsJob, params);
		} catch (Exception e) {
			logger.error("Error executing performArchivePatientRecordsJob", e);
		}
	}

	@Scheduled(cron = "0 0 0 1 1 *", zone = "Asia/Kolkata") // Runs at midnight on January 1st every year
	public void performDeletePatientRecordsJob() {
		logger.info("BatchScheduler - performDeletePatientRecordsJob");
		JobParameters params = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
		try {
			jobLauncher.run(deletePatientRecordsJob, params);
		} catch (Exception e) {
			logger.error("Error executing performDeletePatientRecordsJob", e);
		}
	}

//	@Scheduled(cron = "0 0 18 * * ?")
//	public void performAppointmentReminderAMJob() {
//		logger.info("BatchScheduler - performAppointmentReminderAMJob");
//		JobParameters params = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
//		try {
//			jobLauncher.run(appointmentReminderJob, params);
//		} catch (Exception e) {
//			logger.error("Error executing appointmentReminderJob", e);
//		}
//	}

//	@Scheduled(cron = "0 0 0 * * ?")
//	public void performJob() {
//		logger.info("BatchScheduler - performDeleteExpiredSchedulesJob");
//		JobParameters params = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
//		try {
//			jobLauncher.run(deleteExpiredSchedulesJob, params);
//		} catch (Exception e) {
//			logger.error("Error executing deleteExpiredSchedulesJob", e);
//		}
//	}

}