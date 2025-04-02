package com.healthcaremngnt.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import com.healthcaremngnt.job.processor.AppointmentRemainderPMItemProcessor;
import com.healthcaremngnt.job.reader.AppointmentReportItemReader;
import com.healthcaremngnt.job.tasklet.AppointmentNoShowTasklet;
import com.healthcaremngnt.job.tasklet.AppointmentReportEmailTasklet;
import com.healthcaremngnt.job.tasklet.ArchivePatientRecordsTasklet;
import com.healthcaremngnt.job.tasklet.DeleteExpiredSchedulesTasklet;
import com.healthcaremngnt.job.tasklet.DeletePatientRecordsTasklet;
import com.healthcaremngnt.job.writer.AppointmentReportItemWriter;
import com.healthcaremngnt.model.Appointment;

@Configuration
@EnableBatchProcessing
@ComponentScan(basePackages = "com.healthcaremngnt")
@Import(DataSourceConfig.class)
@EnableScheduling
public class BatchConfig {

	private static final Logger logger = LogManager.getLogger(BatchConfig.class);

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private AppointmentReportItemReader appointmentItemReader;

	@Autowired
	private AppointmentRemainderPMItemProcessor appointmentItemProcessor;

	@Autowired
	private AppointmentReportItemWriter appointmentItemWriter;

	@Autowired
	AppointmentReportEmailTasklet appointmentReportEmailTasklet;

	@Autowired
	private DeleteExpiredSchedulesTasklet deleteExpiredSchedulesTasklet;

	@Autowired
	private AppointmentNoShowTasklet appointmentNoShowTasklet;

	@Autowired
	private ArchivePatientRecordsTasklet archivePatientRecordsTasklet;
	
	@Autowired
	private DeletePatientRecordsTasklet deletePatientRecordsTasklet;

	@Bean
	public JobLauncher jobLauncher() throws Exception {
		TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}

	@Bean
	public Job appointmentReportJob() {
		logger.info("BatchConfig - appointmentReportJob");
		return new JobBuilder("appointmentReportJob", jobRepository).start(generateReportStep()).on("NO_DATA").end()
				.from(generateReportStep()).on("COMPLETED").to(sendEmailStep()).end().build();
	}

	@Bean
	public Step generateReportStep() {
		logger.info("BatchConfig - generateReportStep");
		return new StepBuilder("generateReportStep", jobRepository)
				.<Appointment, Appointment>chunk(10, transactionManager).reader(appointmentItemReader)
				.processor(appointmentItemProcessor).writer(appointmentItemWriter)
				.listener(new StepExecutionListener() {
					@Override
					public void beforeStep(StepExecution stepExecution) {
						logger.info("Starting generateReportStep");
						appointmentItemWriter.resetState();
					}

					@Override
					public ExitStatus afterStep(StepExecution stepExecution) {
						stepExecution.getJobExecution().getExecutionContext().putString("reportGenerated",
								String.valueOf(appointmentItemWriter.isReportGenerated()));
						logger.debug("reportGenerated: {}", appointmentItemWriter.isReportGenerated());
						if (!appointmentItemWriter.isReportGenerated()) {
							return new ExitStatus("NO_DATA");
						}
						return ExitStatus.COMPLETED;

					}
				}).build();
	}

	@Bean
	public Step sendEmailStep() {
		logger.info("BatchConfig - sendEmailStep");
		return new StepBuilder("sendEmailStep", jobRepository)
				.tasklet(appointmentReportEmailTasklet, transactionManager).build();
	}

	// Delete doctor schedules having schedule date < current date at 12 am.
	@Bean
	public Job deleteExpiredSchedulesJob() {
		logger.info("BatchConfig - deleteExpiredSchedulesJob");
		return new JobBuilder("deleteExpiredSchedulesJob", jobRepository).start(deleteExpiredSchedulesStep()).build();
	}

	@Bean
	public Step deleteExpiredSchedulesStep() {
		logger.info("BatchConfig - deleteExpiredSchedulesStep");
		return new StepBuilder("deleteExpiredSchedulesStep", jobRepository)
				.tasklet(deleteExpiredSchedulesTasklet, transactionManager).transactionManager(transactionManager)
				.build();
	}

	// Change the Appointment status to ‘NoShow’ if the patient doesn’t show up for
	// the appointment
	@Bean
	public Job appointmentNoShowJob() {
		logger.info("BatchConfig - appointmentNoShowJob");
		return new JobBuilder("appointmentNoShowJob", jobRepository).start(appointmentNoShowStep()).build();
	}

	@Bean
	public Step appointmentNoShowStep() {
		logger.info("BatchConfig - appointmentNoShowStep");
		return new StepBuilder("appointmentNoShowStep", jobRepository)
				.tasklet(appointmentNoShowTasklet, transactionManager).transactionManager(transactionManager).build();
	}

	// Should archive inactive patient profiles every 6 months
	@Bean
	public Job archivePatientRecordsJob() {
		logger.info("BatchConfig - archivePatientRecordsJob");
		return new JobBuilder("archivePatientRecordsJob", jobRepository).start(archivePatientRecordsStep()).build();
	}

	@Bean
	public Step archivePatientRecordsStep() {
		logger.info("BatchConfig - archivePatientRecordsStep");
		return new StepBuilder("archivePatientRecordsStep", jobRepository)
				.tasklet(archivePatientRecordsTasklet, transactionManager).transactionManager(transactionManager)
				.build();
		// To-Do ------> Notification to Patients: Before archiving, consider notifying
		// patients about their profile's inactive status. This gives them an
		// opportunity to reactivate their profile if needed.
	}

	// Should delete archived patient profiles every 1 year
	@Bean
	public Job deletePatientRecordsJob() {
		logger.info("BatchConfig - deletePatientRecordsJob");
		return new JobBuilder("deletePatientRecordsJob", jobRepository).start(deletePatientRecordsStep()).build();
	}

	@Bean
	public Step deletePatientRecordsStep() {
		logger.info("BatchConfig - deletePatientRecordsStep");
		return new StepBuilder("deletePatientRecordsStep", jobRepository)
				.tasklet(deletePatientRecordsTasklet, transactionManager).transactionManager(transactionManager)
				.build();
		// To-Do ------> Notification to Patients: Before archiving, consider notifying
		// patients about their profile's inactive status. This gives them an
		// opportunity to reactivate their profile if needed.
	}

}