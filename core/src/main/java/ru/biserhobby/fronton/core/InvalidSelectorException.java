package ru.biserhobby.fronton.core;

public class InvalidSelectorException extends FrontonException {
	public InvalidSelectorException() {
	}

	public InvalidSelectorException(String message) {
		super(message);
	}

	public InvalidSelectorException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidSelectorException(Throwable cause) {
		super(cause);
	}

	public InvalidSelectorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
