package com.healthcaremngnt.service;

import com.healthcaremngnt.model.PasswordResetToken;

public interface PasswordResetTokenService {

	PasswordResetToken findByToken(String token);

	PasswordResetToken getLatestTokenForUser(String emailID);

}