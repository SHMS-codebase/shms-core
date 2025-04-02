package com.healthcaremngnt.exceptions;

public class PrescriptionNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public PrescriptionNotFoundException(String message) {
		super(message);
	}
	
}