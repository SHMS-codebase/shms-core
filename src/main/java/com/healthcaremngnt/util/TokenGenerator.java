package com.healthcaremngnt.util;

import java.security.SecureRandom;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.healthcaremngnt.constants.MessageConstants;

public class TokenGenerator {

	private static final Logger logger = LogManager.getLogger(TokenGenerator.class);

	private TokenGenerator() {
		// Private constructor to prevent instantiation
		throw new UnsupportedOperationException("Utility class - instantiation not allowed");
	}
	
	private static final SecureRandom secureRandom = new SecureRandom();
	private static final int TOKEN_BYTE_LENGTH = 32; // 256 bits

	public static String generateToken() {
		try {
			
			byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
			secureRandom.nextBytes(tokenBytes);
			String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
			logger.debug("Generated secure token for password reset: {}", token);
			return token;
		} catch (Exception e) {
			logger.error("{}: {}", MessageConstants.TOKEN_GEN_ERROR, e);
			throw new RuntimeException(MessageConstants.TOKEN_GEN_ERROR, e);
		}
	}

}