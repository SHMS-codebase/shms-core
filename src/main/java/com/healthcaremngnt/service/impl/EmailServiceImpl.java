package com.healthcaremngnt.service.impl;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.constants.SmartHealthCareConstants;
import com.healthcaremngnt.exceptions.EmailSendException;
import com.healthcaremngnt.model.Appointment;
import com.healthcaremngnt.model.Doctor;
import com.healthcaremngnt.model.PasswordResetToken;
import com.healthcaremngnt.model.Patient;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.repository.PasswordResetTokenRepository;
import com.healthcaremngnt.service.EmailService;
import com.healthcaremngnt.util.TokenGenerator;

import jakarta.mail.internet.MimeMessage;

@Service
@Transactional
public class EmailServiceImpl implements EmailService {

	private static final Logger logger = LogManager.getLogger(EmailServiceImpl.class);

	private final JavaMailSender mailSender;
	private final PasswordResetTokenRepository tokenRepository;

	public EmailServiceImpl(JavaMailSender mailSender, PasswordResetTokenRepository tokenRepository) {
		this.mailSender = mailSender;
		this.tokenRepository = tokenRepository;
	}

	@Override
	public boolean sendPasswordResetEmail(String emailID, User user) {
		logger.info("Sending password reset email to: {}", emailID);

		try {
			PasswordResetToken resetToken = generateAndSavePasswordResetToken(emailID, user);

			String subject = "Password Reset Request";
			String text = String.format(
					"To reset your password, click the link below:\nhttp://localhost:8080/auth/reset-password?token=%s",
					resetToken.getToken());

			sendSimpleEmail(emailID, subject, text);
			return true;

		} catch (Exception e) {
			logger.error("Error sending password reset email: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to send password reset email", e);
		}
	}

	private PasswordResetToken generateAndSavePasswordResetToken(String emailID, User user) {
		String token = TokenGenerator.generateToken();

		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setToken(token);
		resetToken.setEmailID(emailID);
		resetToken.setExpirationTime(LocalDateTime.now().plusMinutes(SmartHealthCareConstants.EMAIL_EXPIRATION_LIMIT));
		resetToken.setUser(user);

		return tokenRepository.save(resetToken);
	}

	@Override
	public void sendRegistrationEmail(String emailID, String userName, String generatedUserName,
			String generatedPassword) {
		logger.info("Sending registration email to: {}", emailID);

		try {
			String subject = "Registration Successful";
			String text = String.format(
					"Dear %s,\n\nYour registration is successful!\nUsername: %s\nPassword: %s\n\nBest regards,\nSmart HealthCare Management System",
					userName, generatedUserName, generatedPassword);

			sendSimpleEmail(emailID, subject, text);
		} catch (Exception e) {
			logger.error("Error sending registration email: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to send registration email", e);
		}
	}

	private void sendSimpleEmail(String recipient, String subject, String text) {
		logger.info("Sending email to: {}", recipient);

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(recipient);
		message.setSubject(subject);
		message.setText(text);

		try {
			mailSender.send(message);
			logger.info("Email successfully sent to: {}", recipient);
		} catch (Exception e) {
			logger.error("Error sending email to {}: {}", recipient, e.getMessage(), e);
			throw new RuntimeException("Failed to send email to: " + recipient, e);
		}
	}

	@Override
	public void sendReportsEmail(String jobName, String reportFilePath, String emailID) {

		logger.info("Sending Report email to: {}", emailID);
		logger.debug("Report file path: {}", reportFilePath);

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setTo(emailID);
			if (jobName.equalsIgnoreCase("appointmentReportJob")) {
				helper.setSubject("Smart Healthcare Management System - Appointment Report Attached!");
				helper.setText("Please find the Appointment Report in the attachment.");
			} else if (jobName.equalsIgnoreCase("patientReportJob")) {
				helper.setSubject("Smart Healthcare Management System - Patient Report Attached!");
				helper.setText("Please find the Patient Report in the attachment.");
			} else if (jobName.equalsIgnoreCase("billingReportJob")) {
				helper.setSubject("Smart Healthcare Management System - Billing Report Attached!");
				helper.setText("Please find the Billing Report in the attachment.");
			} else {
				helper.setSubject("Smart Healthcare Management System - Report Attached!");
				helper.setText("Please find the Report in the attachment.");
			}
			attachFile(helper, reportFilePath);

			mailSender.send(message);
		} catch (Exception e) {
			logger.error("Error sending Report email: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to send Report email", e);
		}
	}

	private void attachFile(MimeMessageHelper helper, String filePath) throws Exception {
		File attachment = new File(filePath);
		if (!attachment.exists()) {
			throw new RuntimeException("Attachment file not found: " + filePath);
		}
		helper.addAttachment(attachment.getName(), attachment);
	}

	@Override
	public void sendAppointmentEmail(String emailID, Appointment appointment) {
	    if (appointment == null) {
	        logger.warn("Appointment is null, email will not be sent.");
	        return;
	    }

	    logger.info("Sending Appointment Email to: {}", emailID);
	    logger.debug("Appointment Details: {}", appointment);

	    try {
	        Patient patient = appointment.getPatient();
	        Doctor doctor = appointment.getDoctor();

	        if (patient == null || doctor == null) {
	            logger.error("Patient or Doctor information missing, email will not be sent.");
	            return;
	        }

	        String appointmentDate = String.valueOf(appointment.getAppointmentDate());
	        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a"); // 12-hour format with AM/PM
	        String appointmentTime = appointment.getAppointmentTime().format(timeFormatter);
	        String reasonToVisit = appointment.getReasonToVisit();
	        String subject = "Appointment Notification";

	        String emailContent = String.format(
	            "Dear %s,\n\nYour appointment with %s is scheduled successfully!\n\n"
	            + "Find the appointment details below:\n"
	            + "Appointment Date: %s\nAppointment Time: %s\nReason To Visit: %s\n\n"
	            + "Best regards,\nSmart HealthCare Management System",
	            patient.getPatientName(), doctor.getDoctorName(), appointmentDate, appointmentTime, reasonToVisit
	        );

	        sendSimpleEmail(emailID, subject, emailContent);
	    } catch (Exception e) {
	        logger.error("Error sending appointment email: {}", e.getMessage(), e);
	        throw new EmailSendException("Failed to send appointment email", e);
	    }
	}

	// Appointment Reports sent via Email
//	@Override
//	public void sendAppointmentEmail(String email, String attachmentPath) { // Add attachmentPath parameter
//	    logger.info("Sending appointment email to: {} with attachment: {}", email, attachmentPath);
//
//	    try {
//	        MimeMessage mimeMessage = mailSender.createMimeMessage();
//	        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // true = multipart
//
//	        helper.setTo(email);
//	        helper.setSubject("Daily Appointment Report");
//	        helper.setText("This is your daily appointment report.  Please see the attached file."); // Email body
//
//	        // Add attachment
//	        FileSystemResource file = new FileSystemResource(new File(attachmentPath)); // Create FileSystemResource
//	        helper.addAttachment(file.getFilename(), file); // Add attachment to the email
//
//	        mailSender.send(mimeMessage); // Send the MimeMessage
//	    } catch (MessagingException e) {
//	        logger.error("Error sending appointment email: {}", e.getMessage(), e);
//	        throw new RuntimeException("Failed to send appointment email", e);
//	    } catch (Exception e) { // Catch other potential exceptions (e.g., file not found)
//	        logger.error("Error sending appointment email: {}", e.getMessage(), e);
//	        throw new RuntimeException("Failed to send appointment email", e);
//	    }
//	}

}