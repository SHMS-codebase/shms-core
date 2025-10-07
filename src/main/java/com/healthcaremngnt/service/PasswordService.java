package com.healthcaremngnt.service;

import com.healthcaremngnt.exceptions.InvalidTokenException;
import com.healthcaremngnt.exceptions.TokenExpiredException;

public interface PasswordService {

	void resetPassword(String token, String newpassword) throws InvalidTokenException, TokenExpiredException;

	void resetPassword(Long userID, String password, String newpassword);

}