package com.healthcaremngnt.service;

import com.healthcaremngnt.exceptions.InvalidTokenException;
import com.healthcaremngnt.exceptions.TokenExpiredException;

public interface PasswordResetService {

	void resetPassword(String token, String newpassword) throws InvalidTokenException, TokenExpiredException;

	void resetPassword(Long userID, String password, String newpassword);

}