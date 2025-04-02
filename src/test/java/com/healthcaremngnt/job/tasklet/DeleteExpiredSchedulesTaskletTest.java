package com.healthcaremngnt.job.tasklet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import com.healthcaremngnt.repository.DoctorScheduleRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteExpiredSchedulesTaskletTest {

	@Mock
	private DoctorScheduleRepository doctorScheduleRepository;

	@InjectMocks
	private DeleteExpiredSchedulesTasklet deleteExpiredSchedulesTasklet;

	@Mock
	private StepContribution stepContribution;

	@Mock
	private ChunkContext chunkContext;

	@Test
	public void testExecute() throws Exception {
		// Act
		RepeatStatus status = deleteExpiredSchedulesTasklet.execute(stepContribution, chunkContext);

		// Assert
		verify(doctorScheduleRepository, times(1)).deleteExpiredSchedules();
		assertEquals(RepeatStatus.FINISHED, status);
	}
}
