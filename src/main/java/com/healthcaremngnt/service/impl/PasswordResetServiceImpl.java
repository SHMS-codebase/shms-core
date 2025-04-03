package com.healthcaremngnt.service.impl;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.exceptions.InvalidTokenException;
import com.healthcaremngnt.exceptions.TokenExpiredException;
import com.healthcaremngnt.model.PasswordResetToken;
import com.healthcaremngnt.model.User;
import com.healthcaremngnt.repository.PasswordResetTokenRepository;
import com.healthcaremngnt.repository.UserRepository;
import com.healthcaremngnt.service.PasswordResetService;

@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

	private static final Logger logger = LogManager.getLogger(PasswordResetServiceImpl.class);

	private final UserRepository userRepository;
	private final PasswordResetTokenRepository tokenRepository;
	private final BCryptPasswordEncoder encoder;

	public PasswordResetServiceImpl(UserRepository userRepository, PasswordResetTokenRepository tokenRepository) {
		this.userRepository = userRepository;
		this.tokenRepository = tokenRepository;
		this.encoder = new BCryptPasswordEncoder(12);
	}

	@Override
	public void resetPassword(String token, String newPassword) throws InvalidTokenException, TokenExpiredException {
		logger.info("Resetting password for token: {}", token);

		PasswordResetToken resetToken = validateToken(token);
		User user = resetToken.getUser();

		logger.debug("Token is valid. Updating password for user ID: {}", user.getUserID());

		updateUserPassword(user, newPassword);

		logger.debug("Deleting token after password reset.");
		tokenRepository.delete(resetToken);
	}

	private PasswordResetToken validateToken(String token) throws InvalidTokenException, TokenExpiredException {
		PasswordResetToken resetToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new InvalidTokenException("Invalid password reset token."));

		if (resetToken.getExpirationTime().isBefore(LocalDateTime.now())) {
			throw new TokenExpiredException("Password reset token has expired.");
		}

		return resetToken;
	}

	@Override
	public void resetPassword(Long userID, String oldPassword, String newPassword) {
		logger.info("Resetting password for User ID: {}", userID);

		User user = userRepository.findById(userID)
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + userID));

		validateOldPassword(user, oldPassword);
		updateUserPassword(user, newPassword);
	}

	private void validateOldPassword(User user, String oldPassword) {
		if (!encoder.matches(oldPassword, user.getPassword())) {
			logger.debug("Password mismatch for User ID: {}", user.getUserID());
			throw new RuntimeException("Failed to authenticate: Incorrect old password.");
		}
	}

	private void updateUserPassword(User user, String newPassword) {
		user.setPassword(encoder.encode(newPassword));
		userRepository.save(user);
		logger.info("Password successfully updated for User ID: {}", user.getUserID());
	}

}