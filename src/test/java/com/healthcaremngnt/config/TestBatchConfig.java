package com.healthcaremngnt.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableBatchProcessing
@Import(DataSourceConfig.class)
public class TestBatchConfig {

//	@Autowired
//	private JobRepository jobRepository;

//	@Autowired
//	private PlatformTransactionManager transactionManager;

//	@Bean
//	public JobLauncher jobLauncher() throws Exception {
//		TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
//		jobLauncher.setJobRepository(jobRepository);
//		jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
//		jobLauncher.afterPropertiesSet();
//		return jobLauncher;
//	}

}
