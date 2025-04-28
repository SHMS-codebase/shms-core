package com.healthcaremngnt.config;

//@SpringBootTest(classes = { BatchConfig.class,
//		TestBatchConfig.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@SpringBatchTest
//@ExtendWith(MockitoExtension.class)
public class BatchConfigIntegrationTest {
//
//	@Autowired
//	private JobLauncher jobLauncher;
//
//	@Autowired
//	private Job deleteExpiredSchedulesJob;
//
//	@Mock
//	private DoctorScheduleRepository doctorScheduleRepository;
//
//	@InjectMocks
//	private DeleteExpiredSchedulesTasklet deleteExpiredSchedulesTasklet;
//
//	@Test
//	public void testDeleteExpiredSchedulesJob() throws Exception {
//		doNothing().when(doctorScheduleRepository).deleteExpiredSchedules();
//
//		JobExecution jobExecution = jobLauncher.run(deleteExpiredSchedulesJob,
//				new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters());
//
//		// Wait for the job to complete
//		while (jobExecution.isRunning()) {
//			Thread.sleep(1000);
//		}
//		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
//	}
}
