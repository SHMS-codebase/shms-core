package com.healthcaremngnt.exceptions;

public class EmailRateLimitException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmailRateLimitException(String message) {
		super(message);
	}

}