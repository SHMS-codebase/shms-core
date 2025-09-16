package com.healthcaremngnt;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DriverVersionLogger {
	public static void logDriverVersion() {
		try {
			Driver driver = DriverManager.getDriver("jdbc:mysql://localhost:3306/healthcare_mngnt");
			System.out
					.println("MySQL JDBC Driver Version: " + driver.getMajorVersion() + "." + driver.getMinorVersion());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}