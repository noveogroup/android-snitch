package com.taskadapter.redmineapi;

public class RedMineFormatException extends RedMineCommunicationException {
	private static final long serialVersionUID = 4024202727798727085L;

	public RedMineFormatException(String message) {
		super(message);
	}

	public RedMineFormatException(Throwable cause) {
        super(cause);
    }

	public RedMineFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
