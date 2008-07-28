package org.simject.exception;

public class SimException extends RuntimeException {

	public SimException(String message, Exception cause) {
		super(message, cause);
	}
}
