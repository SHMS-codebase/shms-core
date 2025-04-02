package com.healthcaremngnt.util;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.healthcaremngnt.constants.MessageConstants;

public class TokenGenerator {

	private static final Logger logger = LogManager.getLogger(TokenGenerator.class);

	private TokenGenerator() {
		// Private constructor to prevent instantiation
		throw new UnsupportedOperationException("Utility class - instantiation not allowed");
	}

	public static String generateToken() {
		try {
			String token = UUID.randomUUID().toString();
			logger.debug("Generated token for password reset: {}", token);
			return token;
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.TOKEN_GEN_ERROR, e);
			throw new RuntimeException(MessageConstants.TOKEN_GEN_ERROR, e);
		}
	}

}