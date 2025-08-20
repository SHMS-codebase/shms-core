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
//	private Job expireOutdatedSchedulesJob;
//
//	@Mock
//	private DoctorScheduleRepository doctorScheduleRepository;
//
//	@InjectMocks
//	private ExpireOutdatedSchedulesTasklet expireOutdatedSchedulesTasklet;
//
//	@Test
//	public void testExpireOutdatedSchedulesJob() throws Exception {
//		doNothing().when(doctorScheduleRepository).expireOutdatedSchedules();
//
//		JobExecution jobExecution = jobLauncher.run(expireOutdatedSchedulesJob,
//				new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters());
//
//		// Wait for the job to complete
//		while (jobExecution.isRunning()) {
//			Thread.sleep(1000);
//		}
//		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
//	}
}
