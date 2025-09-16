package com.healthcaremngnt;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@ComponentScan(basePackages = { "com.healthcaremngnt" })
@EnableJpaRepositories(basePackages = "com.healthcaremngnt.repository")
@EntityScan(basePackages = "com.healthcaremngnt.model")
@EnableTransactionManagement
@EnableCaching
public class SmartHealthCareMngntApplication {

	private static final Logger logger = LogManager.getLogger(SmartHealthCareMngntApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(SmartHealthCareMngntApplication.class, args);
		
		Consumer<String> log = msg -> System.out.println("LOG: " + msg);
		log.accept("Application Started");
		
		logger.info("SmartHealthCareMngntApplication");

	}
	
	@PostConstruct
	public void logDriverVersion() {
	    try {
	        Driver driver = DriverManager.getDriver("jdbc:mysql://localhost:3306/your_db");
	        System.out.println("MySQL JDBC Driver Version: " + driver.getMajorVersion() + "." + driver.getMinorVersion());
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

}