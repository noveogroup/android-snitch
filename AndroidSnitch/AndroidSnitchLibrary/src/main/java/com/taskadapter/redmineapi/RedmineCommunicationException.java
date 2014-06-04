package com.taskadapter.redmineapi;

/**
 * Some I/O error
 */
public class RedMineCommunicationException extends RedMineException {
	private static final long serialVersionUID = 8270275922987093576L;

	public RedMineCommunicationException(Throwable cause) {
        super(cause);
    }

	public RedMineCommunicationException(String message) {
		super(message);
	}

	public RedMineCommunicationException(String message, Throwable cause) {
		super(message, cause);
	}
}
