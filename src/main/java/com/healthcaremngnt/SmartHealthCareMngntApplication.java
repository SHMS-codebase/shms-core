package com.healthcaremngnt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackages = { "com.healthcaremngnt" })
@EnableJpaRepositories(basePackages = "com.healthcaremngnt.repository")
@EntityScan(basePackages = "com.healthcaremngnt.model")
@EnableTransactionManagement
public class SmartHealthCareMngntApplication {

	private static final Logger logger = LogManager.getLogger(SmartHealthCareMngntApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(SmartHealthCareMngntApplication.class, args);
		logger.info("SmartHealthCareMngntApplication");

	}

}