package ru.biserhobby.fronton.core;

public class ProjectParsingException extends FrontonException {
	public ProjectParsingException() {
	}

	public ProjectParsingException(String message) {
		super(message);
	}

	public ProjectParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProjectParsingException(Throwable cause) {
		super(cause);
	}

	public ProjectParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
