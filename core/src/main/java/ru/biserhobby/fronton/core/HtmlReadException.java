package ru.biserhobby.fronton.core;

public class HtmlReadException extends FrontonException {
	public HtmlReadException() {
	}

	public HtmlReadException(String message) {
		super(message);
	}

	public HtmlReadException(String message, Throwable cause) {
		super(message, cause);
	}

	public HtmlReadException(Throwable cause) {
		super(cause);
	}

	public HtmlReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
