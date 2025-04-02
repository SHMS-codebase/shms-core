package com.healthcaremngnt.service;

import java.util.Optional;

import com.healthcaremngnt.model.PasswordResetToken;

public interface PasswordResetTokenService {

	Optional<PasswordResetToken> findByToken(String token);

	Optional<PasswordResetToken> getLatestTokenForUser(String emailID);

}