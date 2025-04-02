package com.healthcaremngnt.service;

import com.healthcaremngnt.model.User;

public interface EmailService {

	public boolean sendPasswordResetEmail(String emailID, User user);

	public void sendRegistrationEmail(String emailID, String userName, String generatedUserName, String generatedPassword);

	public void sendAppointmentEmail(String reportFilePath, String emailID);
	
}