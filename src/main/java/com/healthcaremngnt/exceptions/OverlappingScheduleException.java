package com.healthcaremngnt.exceptions;

public class OverlappingScheduleException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public OverlappingScheduleException(String message) {
		super(message);
	}
}