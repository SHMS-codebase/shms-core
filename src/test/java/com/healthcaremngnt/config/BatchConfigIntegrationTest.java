package com.healthcaremngnt.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.healthcaremngnt.job.tasklet.DeleteExpiredSchedulesTasklet;
import com.healthcaremngnt.repository.DoctorScheduleRepository;

@SpringBootTest(classes = { BatchConfig.class,
		TestBatchConfig.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SpringBatchTest
@ExtendWith(MockitoExtension.class)
public class BatchConfigIntegrationTest {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job deleteExpiredSchedulesJob;

	@Mock
	private DoctorScheduleRepository doctorScheduleRepository;

	@InjectMocks
	private DeleteExpiredSchedulesTasklet deleteExpiredSchedulesTasklet;

	@Test
	public void testDeleteExpiredSchedulesJob() throws Exception {
		doNothing().when(doctorScheduleRepository).deleteExpiredSchedules();

		JobExecution jobExecution = jobLauncher.run(deleteExpiredSchedulesJob,
				new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters());

		// Wait for the job to complete
		while (jobExecution.isRunning()) {
			Thread.sleep(1000);
		}
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}
}
