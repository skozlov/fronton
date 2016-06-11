package ru.biserhobby.fronton.core;

public class FrontonException extends RuntimeException {
	public FrontonException() {
	}

	public FrontonException(String message) {
		super(message);
	}

	public FrontonException(String message, Throwable cause) {
		super(message, cause);
	}

	public FrontonException(Throwable cause) {
		super(cause);
	}

	public FrontonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
