package com.healthcaremngnt.exceptions;

public class EmailSendException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmailSendException(String message) {
		super(message);
	}

	public EmailSendException(String message, Throwable cause) {
		super(message, cause);
	}
}