package com.healthcaremngnt.exceptions;

public class AppointmentNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public AppointmentNotFoundException(String message) {
		super(message);
	}
}
