package com.healthcaremngnt.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job appointmentReportJob;

	@Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
	public void runJob() {
		
		try {
			
			JobParameters params = new JobParametersBuilder()
					.addString("JobID", String.valueOf(System.currentTimeMillis())).toJobParameters();
			jobLauncher.run(appointmentReportJob, params);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}