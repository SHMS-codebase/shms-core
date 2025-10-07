package com.healthcaremngnt.config.filter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class EmailRateLimiter {

	private static final Logger logger = LogManager.getLogger(EmailRateLimiter.class);
	private static final int EMAIL_LIMIT = 5;
	private static final Duration WINDOW = Duration.ofHours(1);

	private final Map<String, List<LocalDateTime>> emailLog = new ConcurrentHashMap<>();

	public boolean isEmailAllowed(String emailID) {
		
		logger.info("Checking rate limit for email: {}", emailID);
		LocalDateTime now = LocalDateTime.now();
		List<LocalDateTime> timestamps = emailLog.getOrDefault(emailID, new ArrayList<>());

		// Removing expired timestamps
		timestamps.removeIf(ts -> ts.isBefore(now.minus(WINDOW)));

		if (timestamps.size() >= EMAIL_LIMIT) {
			logger.warn("Rate limit exceeded for email: {} ({} emails in last hour)", emailID, timestamps.size());
			return false;
		}

		timestamps.add(now);
		emailLog.put(emailID, timestamps);
		return true;
	}

}