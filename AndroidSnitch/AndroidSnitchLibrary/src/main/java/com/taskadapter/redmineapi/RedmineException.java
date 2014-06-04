package com.taskadapter.redmineapi;

public class RedMineException extends Exception {
	private static final long serialVersionUID = -1592189045756043062L;

	public RedMineException() {
    }

    public RedMineException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedMineException(String message) {
        super(message);
    }

    public RedMineException(Throwable cause) {
        super(cause);
    }
}
