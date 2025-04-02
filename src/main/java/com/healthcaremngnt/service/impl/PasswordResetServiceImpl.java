package com.healthcaremngnt.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcaremngnt.exceptions.InvalidTokenException;
import com.healthcaremngnt.exceptions.TokenExpiredException;
import com.healthcaremngnt.model.PasswordResetToken;
import com.healthcaremngnt.model.Role;
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
	public void resetPassword(String token, String newpassword) throws InvalidTokenException, TokenExpiredException {

		logger.info("Resetting password for token: {}", token);

		try {
			Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);

			PasswordResetToken resetToken = tokenOptional
					.orElseThrow(() -> new InvalidTokenException("Invalid password reset token."));

			if (resetToken.getExpirationTime().isBefore(LocalDateTime.now())) {
				throw new TokenExpiredException("Password reset token has expired.");
			}

			logger.debug("Token is available in the DB and is valid");

			User user = resetToken.getUser();

			Role role = user.getRole();
			if (role != null) {
				logger.debug("Role Name: {}", role.getRoleName());
			}

			user.setPassword(encoder.encode(newpassword));

			logger.debug("Updating user details in Users table");
			userRepository.save(user);

			logger.debug("Deleting token details from PwdResetTokens table");
			tokenRepository.delete(resetToken); // Invalidate the token after use

		} catch (InvalidTokenException | TokenExpiredException e) {
			logger.error("Password reset failed: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Unexpected error during password reset: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to reset password", e);
		}
	}

	@Override
	public void resetPassword(Long userID, String oldPassword, String newPassword) {

		logger.info("Resetting old password: {} with: {}", oldPassword, newPassword);

		try {

			Optional<User> userOptional = userRepository.findById(userID);

			if (userOptional.isPresent()) {
			
				logger.debug("userOptional: {}", userOptional);
				User user = userOptional.get();
				Role role = user.getRole();

				if (role != null) {
					logger.debug("Role Name: {}", role.getRoleName());
				}
				
				logger.debug("Validate the old password with the Users table");
				
				if (!encoder.matches(oldPassword, user.getPassword())) {
					logger.debug("Failed to authenticate since password does not match stored value");
					throw new RuntimeException("Failed to authenticate since password does not match stored value");
				}

				user.setPassword(encoder.encode(newPassword));

				logger.debug("Updating user details in Users table");
				userRepository.save(user);

			}

		} catch (Exception e) {
			logger.error("Unexpected error during password reset: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to reset password", e);
		}
	}

}