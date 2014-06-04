package com.taskadapter.redmineapi.internal.comm;

import org.apache.http.HttpRequest;
import com.taskadapter.redmineapi.RedMineException;

/**
 * Simple communicator interface.
 * 
 * @author maxkar
 * 
 */
public interface SimpleCommunicator<T> {
	/**
	 * Performs a request.
	 * 
	 * @return the response body.
	 */
	public abstract T sendRequest(HttpRequest request) throws RedMineException;

}
