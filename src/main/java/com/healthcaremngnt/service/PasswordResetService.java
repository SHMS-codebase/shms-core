package com.healthcaremngnt.service;

import com.healthcaremngnt.model.PasswordResetToken;

public interface PasswordResetService {

	PasswordResetToken findByToken(String token);

	PasswordResetToken getLatestTokenForUser(String emailID);

}