package com.healthcaremngnt.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ChunkListener;
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
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import com.healthcaremngnt.job.processor.AppointmentReportItemProcessor;
import com.healthcaremngnt.job.processor.BillingReportItemProcessor;
import com.healthcaremngnt.job.processor.PatientReportItemProcessor;
import com.healthcaremngnt.job.reader.AppointmentReportItemReader;
import com.healthcaremngnt.job.reader.BillingReportItemReader;
import com.healthcaremngnt.job.reader.PatientReportItemReader;
import com.healthcaremngnt.job.tasklet.AppointmentNoShowTasklet;
import com.healthcaremngnt.job.tasklet.ArchivePatientRecordsTasklet;
import com.healthcaremngnt.job.tasklet.DeleteExpiredSchedulesTasklet;
import com.healthcaremngnt.job.tasklet.DeletePatientRecordsTasklet;
import com.healthcaremngnt.job.tasklet.ReportsSendEmailTasklet;
import com.healthcaremngnt.job.writer.AppointmentReportItemWriter;
import com.healthcaremngnt.job.writer.BillingReportItemWriter;
import com.healthcaremngnt.job.writer.PatientReportItemWriter;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.Invoice;
import com.healthcaremngnt.model.Patient;

@Configuration
@EnableBatchProcessing
@ComponentScan(basePackages = "com.healthcaremngnt")
//@Import(DataSourceConfig.class)
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
	private AppointmentReportItemProcessor appointmentItemProcessor;

	@Autowired
	private AppointmentReportItemWriter appointmentItemWriter;

	@Autowired
	private PatientReportItemReader patientItemReader;

	@Autowired
	private PatientReportItemProcessor patientItemProcessor;

	@Autowired
	private PatientReportItemWriter patientItemWriter;

	@Autowired
	private BillingReportItemReader billingItemReader;

	@Autowired
	private BillingReportItemProcessor billingItemProcessor;

	@Autowired
	private BillingReportItemWriter billingItemWriter;

	@Autowired
	private ReportsSendEmailTasklet reportsSendEmailTasklet;

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
		return new JobBuilder("appointmentReportJob", jobRepository).start(generateAppointmentReportStep())
				.on("NO_DATA").end().from(generateAppointmentReportStep()).on("COMPLETED").to(sendEmailStep()).end()
				.build();
	}

	@Bean
	public Step generateAppointmentReportStep() {
		logger.info("BatchConfig - generateAppointmentReportStep");
		return new StepBuilder("generateAppointmentReportStep", jobRepository)
				.<Appointment, Appointment>chunk(10, transactionManager).reader(appointmentItemReader)
				.processor(appointmentItemProcessor).writer(appointmentItemWriter).listener(new ChunkListener() {
					@Override
					public void beforeChunk(ChunkContext context) {
						logger.info("Starting generateAppointmentReportStep chunk");
						// Reset state at the beginning of each chunk if needed
						// Note: If you need to reset only once per step, consider keeping a
						// StepListener too
						appointmentItemWriter.resetState();
					}

					@Override
					public void afterChunk(ChunkContext context) {
						StepExecution stepExecution = context.getStepContext().getStepExecution();
						stepExecution.getJobExecution().getExecutionContext().putString("reportGenerated",
								String.valueOf(appointmentItemWriter.isReportGenerated()));
						logger.debug("reportGenerated: {}", appointmentItemWriter.isReportGenerated());
					}

					@Override
					public void afterChunkError(ChunkContext context) {
						// Handle any error logic here if needed
					}
				}).listener(new StepExecutionListener() {
					@Override
					public void beforeStep(StepExecution stepExecution) {
						logger.info("Starting generateAppointmentReportStep stepListener");
						// Any step-level setup can remain here
					}

					@Override
					public ExitStatus afterStep(StepExecution stepExecution) {
						// The exit status logic needs to remain in the StepExecutionListener
						if (!appointmentItemWriter.isReportGenerated()) {
							return new ExitStatus("NO_DATA");
						}
						return ExitStatus.COMPLETED;

					}
				}).build();
	}

	@Bean
	public Job patientReportJob() {
		logger.info("BatchConfig - patientReportJob");
		return new JobBuilder("patientReportJob", jobRepository).start(generatePatientReportStep()).on("NO_DATA").end()
				.from(generatePatientReportStep()).on("COMPLETED").to(sendEmailStep()).end().build();
	}

	@Bean
	public Step generatePatientReportStep() {
		logger.info("BatchConfig - generatePatientReportStep");
		return new StepBuilder("generatePatientReportStep", jobRepository)
				.<Patient, Patient>chunk(10, transactionManager).reader(patientItemReader)
				.processor(patientItemProcessor).writer(patientItemWriter).listener(new ChunkListener() {
					@Override
					public void beforeChunk(ChunkContext context) {
						logger.info("Starting generatePatientReportStep chunk");
						// Reset state at the beginning of each chunk if needed
						// Note: If you need to reset only once per step, consider keeping a
						// StepListener too
						patientItemWriter.resetState();
					}

					@Override
					public void afterChunk(ChunkContext context) {
						StepExecution stepExecution = context.getStepContext().getStepExecution();
						stepExecution.getJobExecution().getExecutionContext().putString("reportGenerated",
								String.valueOf(patientItemWriter.isReportGenerated()));
						logger.debug("reportGenerated: {}", patientItemWriter.isReportGenerated());
					}

					@Override
					public void afterChunkError(ChunkContext context) {
						// Handle any error logic here if needed
					}
				}).listener(new StepExecutionListener() {
					@Override
					public void beforeStep(StepExecution stepExecution) {
						logger.info("Starting generatePatientReportStep stepListener");
						// Any step-level setup can remain here
					}

					@Override
					public ExitStatus afterStep(StepExecution stepExecution) {
						// The exit status logic needs to remain in the StepExecutionListener
						if (!patientItemWriter.isReportGenerated()) {
							return new ExitStatus("NO_DATA");
						}
						return ExitStatus.COMPLETED;

					}
				}).build();
	}

	@Bean
	public Job billingReportJob() {
		logger.info("BatchConfig - billingReportJob");
		return new JobBuilder("billingReportJob", jobRepository).start(generateBillingReportStep()).on("NO_DATA").end()
				.from(generateBillingReportStep()).on("COMPLETED").to(sendEmailStep()).end().build();
	}

	@Bean
	public Step generateBillingReportStep() {
		logger.info("BatchConfig - generateBillingReportStep");
		return new StepBuilder("generateBillingReportStep", jobRepository)
				.<Invoice, Invoice>chunk(10, transactionManager).reader(billingItemReader)
				.processor(billingItemProcessor).writer(billingItemWriter).listener(new ChunkListener() {
					@Override
					public void beforeChunk(ChunkContext context) {
						logger.info("Starting generateBillingReportStep chunk");
						// Reset state at the beginning of each chunk if needed
						// Note: If you need to reset only once per step, consider keeping a
						// StepListener too
						billingItemWriter.resetState();
					}

					@Override
					public void afterChunk(ChunkContext context) {
						StepExecution stepExecution = context.getStepContext().getStepExecution();
						stepExecution.getJobExecution().getExecutionContext().putString("reportGenerated",
								String.valueOf(billingItemWriter.isReportGenerated()));
						logger.debug("reportGenerated: {}", billingItemWriter.isReportGenerated());
					}

					@Override
					public void afterChunkError(ChunkContext context) {
						// Handle any error logic here if needed
					}
				}).listener(new StepExecutionListener() {
					@Override
					public void beforeStep(StepExecution stepExecution) {
						logger.info("Starting generateBillingReportStep stepListener");
						// Any step-level setup can remain here
					}

					@Override
					public ExitStatus afterStep(StepExecution stepExecution) {
						// The exit status logic needs to remain in the StepExecutionListener
						if (!billingItemWriter.isReportGenerated()) {
							return new ExitStatus("NO_DATA");
						}
						return ExitStatus.COMPLETED;

					}
				}).build();
	}

	@Bean
	public Step sendEmailStep() {
		logger.info("BatchConfig - sendEmailStep");
		return new StepBuilder("sendEmailStep", jobRepository).tasklet(reportsSendEmailTasklet, transactionManager)
				.build();
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