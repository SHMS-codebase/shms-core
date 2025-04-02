package com.healthcaremngnt.exceptions;

public class UsernameAlreadyExistsException extends Exception {

	private static final long serialVersionUID = 1L;

	public UsernameAlreadyExistsException(String message) {
		super(message);
	}

	public UsernameAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

}