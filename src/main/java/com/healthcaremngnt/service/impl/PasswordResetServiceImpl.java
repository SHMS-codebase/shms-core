package com.healthcaremngnt.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.model.PasswordResetToken;
import com.healthcaremngnt.repository.PasswordResetTokenRepository;
import com.healthcaremngnt.service.PasswordResetService;

@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

	private static final Logger logger = LogManager.getLogger(PasswordResetServiceImpl.class);

	private final PasswordResetTokenRepository tokenRepository;

	public PasswordResetServiceImpl(PasswordResetTokenRepository tokenRepository) {
		this.tokenRepository = tokenRepository;
	}

	@Override
	public PasswordResetToken findByToken(String token) {
		logger.info("Fetching password reset token: {}", token);

		return tokenRepository.findByToken(token)
				.orElseThrow(() -> new RuntimeException("Invalid password reset token: " + token));
	}

	@Override
	public PasswordResetToken getLatestTokenForUser(String emailID) {
		logger.info("Fetching latest password reset token for user with email: {}", emailID);

		return tokenRepository.findFirstByEmailIDOrderByTokenIDDesc(emailID)
				.orElseThrow(() -> new RuntimeException("No password reset token found for email: " + emailID));
	}

}