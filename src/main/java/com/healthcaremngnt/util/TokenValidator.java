package com.healthcaremngnt.util;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;

import com.healthcaremngnt.constants.MessageConstants;
import com.healthcaremngnt.model.PasswordResetToken;

public class TokenValidator {

	private static final Logger logger = LogManager.getLogger(TokenValidator.class);

	private TokenValidator() {
		// Private constructor to prevent instantiation
		throw new UnsupportedOperationException("Utility class - instantiation not allowed");
	}

	public static String validateResetToken(PasswordResetToken resetToken, Model model) {
		logger.debug("Validating Reset Token");

		if (resetToken == null) {
			logger.warn("Reset token is null");
			model.addAttribute("message", MessageConstants.INVALID_TOKEN);
			return "error";
		}

		if (resetToken.getExpirationTime().isBefore(LocalDateTime.now())) {
			logger.warn("Reset token has expired");
			model.addAttribute("message", MessageConstants.EXPIRED_TOKEN);
			return "error";
		}

		logger.debug("Token validation successful");
		return null; // Indicating the token is valid
	}

}