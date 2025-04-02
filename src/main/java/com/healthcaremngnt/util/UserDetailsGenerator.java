package com.healthcaremngnt.util;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserDetailsGenerator {

	private static final Logger logger = LogManager.getLogger(UserDetailsGenerator.class);

	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final int RANDOM_PART_LENGTH = 8;
	private static final SecureRandom random = new SecureRandom();

	private UserDetailsGenerator() {
		throw new UnsupportedOperationException("Utility class - instantiation not allowed");
	}

	public static String generatePassword(String username, String role) {
		if (username == null || role == null || username.isBlank() || role.isBlank()) {
			throw new IllegalArgumentException("Username and role cannot be null or empty");
		}

		logger.info("Generating password for the user");

		String prefix = getPrefix(username, 2) + getPrefix(role, 2);
		String randomChars = generateRandomChars(RANDOM_PART_LENGTH);

		return prefix + randomChars;
	}

	public static String generateUserName(String doctorName, String roleName) {
		if (doctorName == null || roleName == null || doctorName.isBlank() || roleName.isBlank()) {
			throw new IllegalArgumentException("Doctor name and role name cannot be null or empty");
		}

		logger.info("Generating username for the user");

		String prefix = getPrefix(doctorName, 2) + getPrefix(roleName, 2);
		String randomChars = generateRandomChars(2);

		return prefix + randomChars;
	}

	private static String getPrefix(String input, int length) {
		return input.length() > length ? input.substring(0, length) : input;
	}

	private static String generateRandomChars(int length) {
		return IntStream.range(0, length)
				.mapToObj(i -> String.valueOf(CHARACTERS.charAt(random.nextInt(CHARACTERS.length()))))
				.collect(Collectors.joining());
	}

}