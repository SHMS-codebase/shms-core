package com.healthcaremngnt.service.impl;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.model.PasswordResetToken;
import com.healthcaremngnt.repository.PasswordResetTokenRepository;
import com.healthcaremngnt.service.PasswordResetTokenService;

@Service
@Transactional
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

	private static final Logger logger = LogManager.getLogger(PasswordResetTokenServiceImpl.class);

	private final PasswordResetTokenRepository tokenRepository;

	public PasswordResetTokenServiceImpl(PasswordResetTokenRepository tokenRepository) {
		this.tokenRepository = tokenRepository;
	}

	@Override
	public Optional<PasswordResetToken> findByToken(String token) {

		logger.info("Finding PasswordResetToken by token: {}", token);

		return tokenRepository.findByToken(token);
	}

	@Override
	public Optional<PasswordResetToken> getLatestTokenForUser(String emailID) {

		logger.info("Getting latest PasswordResetToken for user with email: {}", emailID);

		return tokenRepository.findFirstByEmailIDOrderByTokenIDDesc(emailID);
	}

}