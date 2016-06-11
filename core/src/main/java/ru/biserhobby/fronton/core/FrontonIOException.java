package ru.biserhobby.fronton.core;

public class FrontonIOException extends FrontonException {
	public FrontonIOException() {
	}

	public FrontonIOException(String message) {
		super(message);
	}

	public FrontonIOException(String message, Throwable cause) {
		super(message, cause);
	}

	public FrontonIOException(Throwable cause) {
		super(cause);
	}

	public FrontonIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
