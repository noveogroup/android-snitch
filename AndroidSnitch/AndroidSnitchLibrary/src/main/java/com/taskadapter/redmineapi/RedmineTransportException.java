package com.taskadapter.redmineapi;

public class RedMineTransportException extends RedMineCommunicationException {
	private static final long serialVersionUID = 3463778589975943695L;

	public RedMineTransportException(Throwable cause) {
        super(cause);
    }

	public RedMineTransportException(String message, Throwable cause) {
		super(message, cause);
	}

	public RedMineTransportException(String message) {
		super(message);
	}

}
