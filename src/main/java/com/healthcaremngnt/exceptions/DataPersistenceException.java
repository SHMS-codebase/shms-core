package com.healthcaremngnt.exceptions;

public class DataPersistenceException extends Exception {

	private static final long serialVersionUID = 1L;

	public DataPersistenceException(String message, Exception e) {
		super(message);
	}

	public DataPersistenceException(String message) {
		super(message);
	}
}
