package com.taskadapter.redmineapi;

/**
 * User or password (or API access key) not recognized.
 */
public class RedMineAuthenticationException extends RedMineSecurityException {
	private static final long serialVersionUID = -2494397318821827279L;

	public RedMineAuthenticationException(String message) {
        super(message);
    }
}
